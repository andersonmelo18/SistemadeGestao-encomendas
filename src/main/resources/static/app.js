'use strict';
const $ = (s, root = document) => root.querySelector(s);
const $$ = (s, root = document) => [...root.querySelectorAll(s)];
const stages = ['EM_ANALISE','APROVADO','FILA','EM_PRODUCAO','FINALIZADO'];
const labels = {EM_ANALISE:'Em análise', APROVADO:'Aprovado', FILA:'Na fila', EM_PRODUCAO:'Em produção', FINALIZADO:'Finalizado',
 ADMIN:'Administrador', ATENDENTE:'Atendente', PRODUCAO:'Produção', CLIENTE:'Cliente', ENTREGA:'Entrega', TECNICO:'Técnico'};
const money = n => new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(n);
const date = s => s ? new Date(s).toLocaleString('pt-BR') : '';
const esc = s => String(s ?? '').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
let auth = '', me = null, view = '', page = 0, filter = '', generation = 0, refreshing = false;
const modal = $('#modal');
const badge = status => '<span class="badge '+esc(status)+'">'+esc(labels[status] || status)+'</span>';
const canEdit = () => ['ADMIN','ATENDENTE'].includes(me?.perfil);
const canAdvance = () => ['ADMIN','PRODUCAO'].includes(me?.perfil);
const baseOrders = () => me.perfil === 'CLIENTE' ? '/api/meus-pedidos' : '/api/pedidos';

async function api(path, options = {}) {
  const headers = {'X-Requested-With':'XMLHttpRequest', ...options.headers};
  if (auth) headers.Authorization = 'Basic '+auth;
  if (options.body) headers['Content-Type'] = 'application/json';
  if (options.method && options.method !== 'GET') {
    const token = await api('/api/acesso/csrf');
    headers[token.headerName] = token.token;
  }
  let response;
  try { response = await fetch(path, {...options, headers, credentials:'same-origin', cache:'no-store'}); }
  catch { throw new Error('Não foi possível conectar ao servidor. Verifique se a aplicação está em execução.'); }
  const data = response.status === 204 ? null : await response.json().catch(()=>null);
  if (!response.ok) {
    const messages = {401:'Celular ou senha inválidos, ou cadastro inativo.',403:'Você não tem permissão para esta operação.',404:'Registro não encontrado.',409:'Este registro foi alterado. Feche esta janela e abra o pedido novamente antes de tentar salvar.'};
    const e = new Error(data?.erros ? Object.values(data.erros).join('\n') : data?.detail || messages[response.status] || 'Não foi possível concluir a operação.');
    e.status = response.status;
    if (response.status === 401 && me) { reset(); $('#login-error').textContent = 'Seu acesso expirou ou foi desativado. Entre novamente.'; }
    throw e;
  }
  return data;
}
function notice(message, error=false) {
  $('#notice').hidden = false; $('#notice').textContent = message; $('#notice').className = error ? 'failure' : '';
}
function openModal(title, body) {
  $('#modal-title').textContent = title; $('#modal-body').innerHTML = body; $('#modal-error').textContent = '';
  if (!modal.open) modal.showModal();
}
function reset() {
  generation++; auth=''; me=null; modal.close(); $('#app').hidden=true; $('#login-screen').hidden=false;
  $('#content').replaceChildren(); $('#navigation').replaceChildren(); $('#login-form').reset();
}
async function formSubmit(form, fn) {
  form.addEventListener('submit', async event=>{
    event.preventDefault(); if (form.dataset.busy) return;
    form.dataset.busy='true'; const buttons=$$('button',form); buttons.forEach(b=>b.disabled=true);
    $('#modal-error').textContent='';
    try { await fn(new FormData(form)); }
    catch(e) { if(modal.open) $('#modal-error').textContent=e.message; else if(me) notice(e.message,true); }
    finally { delete form.dataset.busy; buttons.forEach(b=>b.disabled=false); }
  });
}
$('#close-modal').onclick=()=>modal.close();
$('#login-form').addEventListener('submit',async event=>{
  event.preventDefault(); const form=event.currentTarget, button=$('button',form);
  button.disabled=true; $('#login-error').textContent='';
  const values=new FormData(form), celular=values.get('celular').replace(/\D/g,'');
  auth=btoa(String.fromCharCode(...new TextEncoder().encode(celular+':'+values.get('senha'))));
  try {
    me=await api('/api/acesso/me'); form.reset(); $('#login-screen').hidden=true; $('#app').hidden=false;
    $('#account-name').textContent=me.nome; $('#account-role').textContent=labels[me.perfil];
    const tabs=me.perfil==='ADMIN'?[['orders','Pedidos'],['production','Produção'],['clients','Clientes'],['users','Equipe']]:
      me.perfil==='ATENDENTE'?[['orders','Pedidos'],['clients','Clientes']]:
      me.perfil==='PRODUCAO'?[['production','Produção']]:me.perfil==='CLIENTE'?[['orders','Meus pedidos']]:[['pending','Meu painel']];
    $('#navigation').innerHTML=tabs.map(([id,name])=>'<button data-view="'+id+'">'+name+'</button>').join('');
    $$('[data-view]').forEach(b=>b.onclick=()=>navigate(b.dataset.view)); navigate(tabs[0][0]);
  } catch(e) { auth=''; me=null; $('#login-error').textContent=e.message; }
  finally { button.disabled=false; }
});
$('#logout').onclick=async()=>{
  try { await api('/logout',{method:'POST'}); } catch { /* Limpar o acesso local mesmo sem rede. */ }
  reset();
};
function navigate(next) {
  generation++; view=next; page=0; filter=''; $('#notice').hidden=true;
  $$('[data-view]').forEach(b=>b.classList.toggle('active',b.dataset.view===view));
  const title={orders:me.perfil==='CLIENTE'?'Meus pedidos':'Pedidos',production:'Painel de produção',clients:'Clientes',users:'Equipe',pending:'Área de entrega'};
  $('#area-label').textContent=labels[me.perfil]; $('#page-title').textContent=title[view] || 'Meu painel';
  $('#page-description').textContent=({orders:me.perfil==='CLIENTE'?'Acompanhe cada etapa. Para alterações, fale com o atendente.':'Lance encomendas e acompanhe o andamento de cada pedido.',production:'Todos os produtos avançam juntos, na etapa do pedido.',clients:'Cadastros e acessos dos seus clientes.',users:'Cadastre as pessoas que vão operar cada área.',pending:'O acesso está ativo. O controle de entrega será disponibilizado na próxima etapa.'})[view];
  $('#page-actions').innerHTML=['orders','production','clients'].includes(view)?'<button id="refresh">Atualizar</button>':'';
  if ($('#refresh')) $('#refresh').onclick=()=>load();
  if(view==='orders' && canEdit()) addAction('Novo pedido',()=>orderForm());
  if(view==='clients') addAction('Novo cliente',()=>clientForm());
  if(view==='users') addAction('Cadastrar usuário',()=>userForm());
  load();
}
function addAction(text,fn) { const b=document.createElement('button'); b.className='primary'; b.textContent=text; b.onclick=()=>Promise.resolve(fn()).catch(e=>notice(e.message,true)); $('#page-actions').append(b); }
async function load(quiet=false) {
  if(!me) return;
  const current=++generation, selected=view;
  if(!quiet) $('#content').innerHTML='<div class="panel" role="status">Carregando…</div>';
  try {
    if(['orders','production'].includes(view)) {
      const data=await api(baseOrders()+'?page='+page+'&size=12&sort=id,desc'+(filter?'&status='+filter:''));
      if(current!==generation || !me) return;
      renderOrders(data);
    } else if(view==='clients') {
      const data=await api('/clientes?page='+page+'&size=10&sort=id,desc');
      if(current!==generation || !me) return; renderClients(data);
    } else if(view==='users') $('#content').innerHTML='<div class="panel"><h2>Acesso por função</h2><p class="muted">Administrador gerencia a operação. Atendente cadastra clientes e pedidos. Produção avança as etapas. Entrega terá seu fluxo em uma próxima etapa.</p><p class="hint">O acesso de cliente é criado junto com o cadastro em Clientes. A listagem de usuários internos ainda não faz parte da API.</p></div>';
    else $('#content').innerHTML='<div class="empty"><h2>Painel em preparação</h2><p class="muted">Seu perfil foi reconhecido. Nenhuma operação de entrega está disponível nesta versão.</p></div>';
  } catch(e) {
    if(current===generation && me) {
      if(!quiet) $('#content').innerHTML='<div class="empty">Não foi possível carregar os dados. Use Atualizar para tentar novamente.</div>';
      notice(e.message,true);
    }
  }
}
function pagination(data) {
  return '<div class="pagination"><button id="previous" '+(data.first?'disabled':'')+'>Anterior</button><span>Página '+(data.number+1)+' de '+Math.max(1,data.totalPages)+'</span><button id="next" '+(data.last?'disabled':'')+'>Próxima</button></div>';
}
function bindPagination() { $('#previous').onclick=()=>{page--;load();}; $('#next').onclick=()=>{page++;load();}; }
function renderOrders(data) {
  const total=data.content.reduce((sum,p)=>sum+Number(p.valorTotal),0);
  $('#content').innerHTML='<div class="summary"><div class="metric"><span>Pedidos no filtro</span><strong>'+data.totalElements+'</strong></div><div class="metric"><span>Pedidos nesta página</span><strong>'+data.numberOfElements+'</strong></div><div class="metric"><span>Valor nesta página</span><strong>'+money(total)+'</strong></div></div>'+
    '<div class="toolbar"><label>Etapa<select id="status-filter"><option value="">Todas as etapas</option>'+stages.map(s=>'<option value="'+s+'" '+(s===filter?'selected':'')+'>'+labels[s]+'</option>').join('')+'</select></label><p class="hint">Atualização a cada 10 s · '+new Date().toLocaleTimeString('pt-BR')+'</p></div>'+
    (data.content.length?'<div class="cards">'+data.content.map(p=>'<article class="order-card"><div class="order-top"><strong>Pedido #'+p.id+'</strong>'+badge(p.status)+'</div><h3>'+esc(p.clienteNome)+'</h3><p class="order-items">'+p.itens.map(i=>i.quantidade+' × '+esc(i.produto)).join('<br>')+'</p><p class="hint">'+date(p.criadoEm)+'</p><div class="order-bottom"><strong>'+money(p.valorTotal)+'</strong><button data-order="'+p.id+'">Ver pedido</button></div></article>').join('')+'</div>':'<div class="empty"><h2>Nenhum pedido por aqui</h2><p class="muted">'+(canEdit()?'Crie um pedido ou escolha outra etapa.':'Os pedidos aparecerão aqui quando forem lançados pelo atendente.')+'</p></div>')+pagination(data);
  $('#status-filter').onchange=e=>{filter=e.target.value;page=0;load();};
  $$('[data-order]').forEach(b=>b.onclick=()=>details(b.dataset.order).catch(e=>notice(e.message,true))); bindPagination();
}
async function details(id) {
  const p=await api(baseOrders()+'/'+id);
  openModal('Pedido #'+p.id,'<div class="detail-meta">'+badge(p.status)+'<strong>'+esc(p.clienteNome)+'</strong><span class="hint">'+date(p.criadoEm)+'</span></div><div class="table-wrap"><table><thead><tr><th>Produto</th><th>Qtd.</th><th>Unitário</th><th>Subtotal</th></tr></thead><tbody>'+p.itens.map(i=>'<tr><td>'+esc(i.produto)+'</td><td>'+i.quantidade+'</td><td>'+money(i.valorUnitario)+'</td><td>'+money(i.subtotal)+'</td></tr>').join('')+'</tbody></table></div><div class="form-footer"><span>Total do pedido</span><strong class="total">'+money(p.valorTotal)+'</strong></div>'+
    (p.observacoes?'<h3>Observações</h3><p class="note">'+esc(p.observacoes)+'</p>':'')+
    '<h3 style="margin-top:1.5rem">Andamento</h3><ol class="timeline">'+stages.map((s,index)=>{const h=p.historico.find(h=>h.novoStatus===s);return '<li class="'+(index<=stages.indexOf(p.status)?'done':'')+'"><strong>'+labels[s]+'</strong><small>'+(h?date(h.dataAlteracao)+' · '+esc(h.responsavel):'Aguardando esta etapa')+'</small></li>';}).join('')+'</ol>'+
    (!p.permiteEdicao?'<p class="note">Este pedido está bloqueado para alterações.</p>':me.perfil==='CLIENTE'?'<p class="note">Precisa de uma alteração? Entre em contato com o atendente antes de o pedido entrar em produção.</p>':'')+
    '<div class="actions">'+(canEdit()&&p.permiteEdicao?'<button id="edit-order">Editar pedido</button>':'')+(canAdvance()&&p.status!=='FINALIZADO'?'<button class="primary" id="advance-order">Avançar para '+labels[stages[stages.indexOf(p.status)+1]]+'</button>':'')+'</div>');
  if($('#edit-order')) $('#edit-order').onclick=()=>orderForm(p).catch(e=>$('#modal-error').textContent=e.message);
  if($('#advance-order')) $('#advance-order').onclick=async event=>{
    const next=stages[stages.indexOf(p.status)+1];
    if(!confirm(next==='EM_PRODUCAO'?'Iniciar produção? Todo o pedido será bloqueado para alterações.':'Confirmar avanço para '+labels[next]+'?')) return;
    event.currentTarget.disabled=true;
    try { await api('/api/pedidos/'+p.id+'/status',{method:'PATCH',body:JSON.stringify({versao:p.versao,status:next})}); await details(p.id); await load(true); }
    catch(e){$('#modal-error').textContent=e.message; if($('#advance-order')) $('#advance-order').disabled=false;}
  };
}
async function activeClients() {
  const result=[]; let n=0, data;
  do { data=await api('/clientes?page='+n+'&size=100&sort=nome,asc'); result.push(...data.content.filter(c=>c.ativo)); n++; } while(!data.last);
  return result;
}
async function orderForm(p=null) {
  const clients=p?[]:await activeClients();
  if(!p&&!clients.length) { notice('Cadastre ou ative um cliente antes de criar o pedido.',true); return; }
  openModal(p?'Editar pedido #'+p.id:'Novo pedido','<form id="order-form">'+(p?'<p>Cliente: <strong>'+esc(p.clienteNome)+'</strong></p>':'<label>Cliente<select name="clienteId" required><option value="">Selecione um cliente</option>'+clients.map(c=>'<option value="'+c.id+'">'+esc(c.nome)+' · '+esc(c.celular||'Sem celular')+'</option>').join('')+'</select></label>')+'<h3>Itens do pedido</h3><div id="items"></div><button type="button" id="add-item">Adicionar produto</button><label style="margin-top:1rem">Observações<textarea name="observacoes" maxlength="2000">'+esc(p?.observacoes||'')+'</textarea></label><div class="form-footer"><div>Total estimado <strong id="form-total" class="total"></strong></div><button class="primary" type="submit">'+(p?'Salvar alterações':'Lançar pedido')+'</button></div></form>');
  function addItem(item={produto:'',quantidade:1,valorUnitario:''}) {
    if($$('.item-row').length>=100) return;
    const row=document.createElement('div'); row.className='item-row';
    row.innerHTML='<label>Produto<input class="product" maxlength="150" required value="'+esc(item.produto)+'"></label><label>Qtd.<input class="quantity" type="number" min="1" max="9999" step="1" required value="'+item.quantidade+'"></label><label>Valor unitário<input class="price" type="number" min=".01" max="99999999.99" step=".01" required value="'+esc(item.valorUnitario)+'"></label><button type="button" aria-label="Remover produto">×</button>';
    $('button',row).onclick=()=>{if($$('.item-row').length>1){row.remove();recalculate();}};
    row.addEventListener('input',recalculate); $('#items').append(row); recalculate();
  }
  function recalculate() { $('#form-total').textContent=money($$('.item-row').reduce((sum,row)=>sum+(Number($('.quantity',row).value)||0)*Math.round((Number($('.price',row).value)||0)*100)/100,0)); }
  (p?.itens || [{}]).forEach(i=>addItem(Object.keys(i).length?i:undefined)); $('#add-item').onclick=()=>addItem();
  formSubmit($('#order-form'),async data=>{
    const body={itens:$$('.item-row').map(row=>({produto:$('.product',row).value.trim(),quantidade:Number($('.quantity',row).value),valorUnitario:Number($('.price',row).value)})),observacoes:data.get('observacoes')||null};
    if(p) body.versao=p.versao; else body.clienteId=Number(data.get('clienteId'));
    const saved=await api('/api/pedidos'+(p?'/'+p.id:''),{method:p?'PUT':'POST',body:JSON.stringify(body)});
    modal.close(); notice(p?'Pedido atualizado.':'Pedido #'+saved.id+' enviado para análise.'); await load(true);
  });
}
function renderClients(data) {
  $('#content').innerHTML=(data.content.length?'<div class="table-wrap"><table><thead><tr><th>Cliente</th><th>Celular</th><th>E-mail</th><th>Situação</th><th>Ações</th></tr></thead><tbody>'+data.content.map(c=>'<tr><td><strong>'+esc(c.nome)+'</strong><br><span class="hint">#'+c.id+(c.cpf?' · CPF '+esc(c.cpf):'')+'</span></td><td>'+esc(c.celular||'Não informado')+'</td><td>'+esc(c.email||'Não informado')+'</td><td>'+ (c.ativo?'Ativo':'Inativo')+'</td><td><div class="actions"><button data-client="'+c.id+'">Editar</button><button data-toggle="'+c.id+'">'+(c.ativo?'Inativar':'Ativar')+'</button></div></td></tr>').join('')+'</tbody></table></div>':'<div class="empty"><h2>Nenhum cliente cadastrado</h2><p class="muted">Comece criando o primeiro acesso de cliente.</p></div>')+pagination(data);
  $$('[data-client]').forEach(b=>b.onclick=async()=>{try{clientForm(await api('/clientes/'+b.dataset.client));}catch(e){notice(e.message,true);}});
  $$('[data-toggle]').forEach(b=>b.onclick=async()=>{
    const c=data.content.find(c=>c.id===Number(b.dataset.toggle));
    if(!confirm((c.ativo?'Inativar':'Ativar')+' o cadastro de '+c.nome+'? '+(c.ativo?'O cliente perderá o acesso.':''))) return;
    b.disabled=true;
    try{await api('/clientes/'+c.id+'/'+(c.ativo?'inativar':'ativar'),{method:'PATCH'});notice('Cadastro atualizado.');await load(true);}catch(e){notice(e.message,true);b.disabled=false;}
  }); bindPagination();
}
function clientForm(c=null) {
  openModal(c?'Editar cliente':'Novo cliente','<form id="client-form"><label>Nome<input name="nome" maxlength="150" required value="'+esc(c?.nome||'')+'"></label>'+
    (!c?'<div class="form-grid"><label>Celular com DDD<input name="celular" type="tel" maxlength="20" required></label><label>Senha inicial<input name="senha" type="password" minlength="8" maxlength="72" autocomplete="new-password" required></label></div>':'<p class="hint">Celular: '+esc(c.celular||'Não informado')+'. Alteração de celular e senha ainda não está disponível.</p>')+
    '<div class="form-grid"><label>E-mail (opcional)<input name="email" type="email" maxlength="150" value="'+esc(c?.email||'')+'"></label>'+(!c?'<label>CPF (opcional)<input name="cpf" maxlength="14"></label>':'')+'</div><button class="primary" type="submit">Salvar cliente</button></form>');
  formSubmit($('#client-form'),async data=>{
    const body={nome:data.get('nome').trim(),email:data.get('email')||null};
    if(!c) Object.assign(body,{celular:data.get('celular').replace(/\D/g,''),senha:data.get('senha'),cpf:data.get('cpf').replace(/\D/g,'')||null});
    await api('/clientes'+(c?'/'+c.id:''),{method:c?'PATCH':'POST',body:JSON.stringify(body)});
    modal.close(); notice(c?'Cliente atualizado.':'Cliente cadastrado. Ele já pode entrar com o celular e a senha definidos.'); await load(true);
  });
}
function userForm() {
  openModal('Cadastrar usuário interno','<form id="user-form"><label>Nome<input name="nome" maxlength="150" required></label><div class="form-grid"><label>Celular com DDD<input name="celular" type="tel" maxlength="20" required></label><label>Senha inicial<input name="senha" type="password" minlength="8" maxlength="72" autocomplete="new-password" required></label></div><label>Perfil<select name="perfil">'+['ATENDENTE','PRODUCAO','ENTREGA','ADMIN'].map(p=>'<option value="'+p+'">'+labels[p]+'</option>').join('')+'</select></label><button class="primary" type="submit">Criar acesso</button></form>');
  formSubmit($('#user-form'),async data=>{
    const body=Object.fromEntries(data);body.celular=body.celular.replace(/\D/g,'');body.nome=body.nome.trim();
    await api('/api/usuarios',{method:'POST',body:JSON.stringify(body)});
    modal.close();notice('Acesso de '+labels[body.perfil]+' criado para '+body.nome+'.');
  });
}
setInterval(async()=>{
  if(!me||modal.open||document.hidden||refreshing||!['orders','production'].includes(view)) return;
  refreshing=true; try {await load(true);} finally {refreshing=false;}
},10000);
