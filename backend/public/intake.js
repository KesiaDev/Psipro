/* PsiPro — Formulário de Intake (vanilla JS) */
(function () {
  'use strict';

  var DRAFT_KEY = 'psipro_intake_draft';
  var STEPS = [
    { id: 1, icon: '👤', title: 'Identificação', sub: 'Seus dados pessoais' },
    { id: 2, icon: '💬', title: 'Queixa Principal', sub: 'O que te trouxe à terapia?' },
    { id: 3, icon: '🏥', title: 'Histórico de Saúde', sub: 'Informações de saúde' },
    { id: 4, icon: '🌿', title: 'Hábitos de Vida', sub: 'Seu estilo de vida' },
    { id: 5, icon: '🔬', title: 'Exames', sub: 'Exames complementares' },
    { id: 6, icon: '🔐', title: 'Consentimento', sub: 'Privacidade e LGPD' },
  ];

  var params = new URLSearchParams(window.location.search);
  var TOKEN = params.get('token');

  var state = {
    step: 1,
    loading: false,
    name: '', birthDate: '', gender: '', profession: '',
    email: '', phone: '',
    queixaPrincipal: '', motivoConsulta: '',
    doencasPrevias: '', medicacoes: '', alergias: '', antecedentesFamiliares: '',
    alimentacao: '', atividadeFisica: '', tabagismo: '', alcool: '',
    examesRealizados: '', resultadosExames: '',
    consentGiven: false,
    nameError: '',
  };

  /* ── Draft ── */
  function loadDraft() {
    if (!TOKEN) return;
    try {
      var d = localStorage.getItem(DRAFT_KEY + '_' + TOKEN);
      if (d) {
        var p = JSON.parse(d);
        Object.keys(p).forEach(function (k) { if (k !== 'consentGiven') state[k] = p[k]; });
      }
    } catch (e) {}
  }
  function saveDraft() {
    if (!TOKEN) return;
    try { localStorage.setItem(DRAFT_KEY + '_' + TOKEN, JSON.stringify(state)); } catch (e) {}
  }

  /* ── Render ── */
  var app = document.getElementById('app');

  function render() {
    if (!TOKEN) { renderError('invalid'); return; }
    app.innerHTML = renderForm();
    bindForm();
  }

  function renderError(type) {
    var icon = type === 'expired' ? '⚠️' : '❌';
    var title = type === 'expired' ? 'Este link expirou' : 'Link inválido';
    var body = type === 'expired'
      ? 'O link de cadastro é válido por 7 dias. Solicite um novo link ao seu terapeuta.'
      : 'Este link não é válido ou já foi utilizado. Solicite um novo link ao seu terapeuta.';
    app.innerHTML = '<div class="screen"><div class="screen-card"><div class="screen-icon">' + icon + '</div><h1 class="screen-title">' + title + '</h1><p class="screen-body">' + body + '</p></div></div>';
  }

  function renderSuccess(firstName) {
    app.innerHTML = '<div class="screen"><div class="screen-card"><div class="screen-icon">✅</div><h1 class="screen-title">Dados enviados com sucesso!</h1><p class="screen-body">Obrigado, <strong>' + esc(firstName) + '</strong>! Seu terapeuta já foi notificado e entrará em contato em breve.</p><div class="screen-foot">Suas informações estão protegidas pela Lei Geral de Proteção de Dados (LGPD).</div></div></div>';
  }

  function renderForm() {
    var step = state.step;
    var total = STEPS.length;
    var progress = Math.round((step / total) * 100);
    var s = STEPS[step - 1];

    return [
      '<header><div class="header-inner">',
        '<div class="header-top"><span class="logo">PsiPro</span><span class="secure-badge">🔒 Formulário Seguro</span></div>',
        '<div class="progress-wrap">',
          '<div class="progress-bar"><div class="progress-fill" style="width:' + progress + '%"></div></div>',
          '<span class="progress-label">' + step + '/' + total + '</span>',
        '</div>',
      '</div></header>',
      '<main>',
        '<div class="step-header"><div class="step-icon">' + s.icon + '</div><div class="step-title">' + s.title + '</div><div class="step-sub">' + s.sub + '</div></div>',
        '<div class="card">' + renderStep(step) + '</div>',
        renderNav(step),
      '</main>',
    ].join('');
  }

  function renderStep(step) {
    if (step === 1) return renderStep1();
    if (step === 2) return renderStep2();
    if (step === 3) return renderStep3();
    if (step === 4) return renderStep4();
    if (step === 5) return renderStep5();
    if (step === 6) return renderStep6();
    return '';
  }

  function renderNav(step) {
    var html = '<div class="nav">';
    if (step > 1) html += '<button class="btn btn-secondary" id="btn-prev">← Anterior</button>';
    if (step < STEPS.length) {
      html += '<button class="btn btn-primary" id="btn-next">' + (step === 5 ? 'Finalizar →' : 'Próximo →') + '</button>';
    }
    html += '</div>';
    if (step > 1 && step === STEPS.length) {
      html += '<button class="btn-back-text" id="btn-prev2">← Voltar</button>';
    }
    return html;
  }

  function field(label, inputHtml, required, error) {
    return '<div class="field"><label class="field-label">' + label + (required ? ' <span class="req">*</span>' : '') + '</label>' + inputHtml + (error ? '<div class="error-msg">' + error + '</div>' : '') + '</div>';
  }

  function inp(type, key, placeholder, attrs) {
    attrs = attrs || '';
    return '<input type="' + type + '" data-key="' + key + '" value="' + esc(state[key]) + '" placeholder="' + esc(placeholder) + '" ' + attrs + ' />';
  }

  function ta(key, placeholder, rows) {
    return '<textarea data-key="' + key + '" rows="' + (rows || 3) + '" placeholder="' + esc(placeholder) + '">' + esc(state[key]) + '</textarea>';
  }

  function sel(key, opts) {
    var html = '<select data-key="' + key + '">';
    opts.forEach(function (o) {
      html += '<option value="' + esc(o.v) + '"' + (state[key] === o.v ? ' selected' : '') + '>' + esc(o.l) + '</option>';
    });
    return html + '</select>';
  }

  function radio(key, opts) {
    return '<div class="radio-group">' + opts.map(function (o) {
      return '<label><input type="radio" name="' + key + '" data-key="' + key + '" value="' + esc(o) + '"' + (state[key] === o ? ' checked' : '') + ' />' + esc(o) + '</label>';
    }).join('') + '</div>';
  }

  function esc(s) { return String(s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }

  function calcIdade(bd) {
    if (!bd) return '';
    var b = new Date(bd), t = new Date();
    var age = t.getFullYear() - b.getFullYear() - (t < new Date(t.getFullYear(), b.getMonth(), b.getDate()) ? 1 : 0);
    return (isNaN(age) || age < 0 || age > 150) ? '' : age + ' anos';
  }

  function renderStep1() {
    var idade = calcIdade(state.birthDate);
    var html = field('Nome completo', inp('text', 'name', 'Ex: Maria da Silva', 'autofocus'), true, state.nameError);
    html += '<div class="field"><label class="field-label">Data de nascimento</label><div class="row">' +
      '<input type="date" data-key="birthDate" value="' + esc(state.birthDate) + '" />' +
      (idade ? '<div class="age-box" style="display:flex;align-items:center;">' + esc(idade) + '</div>' : '') +
      '</div></div>';
    html += '<div class="field"><label class="field-label">Sexo</label>' + radio('gender', ['Masculino', 'Feminino', 'Outro']) + '</div>';
    html += field('Profissão', inp('text', 'profession', 'Ex: Professora'));
    html += '<div class="field"><label class="field-label">E-mail</label>' + inp('email', 'email', 'seu@email.com') + '</div>';
    html += '<div class="field"><label class="field-label">Telefone</label>' + inp('tel', 'phone', '(11) 99999-0000') + '</div>';
    return html;
  }

  function renderStep2() {
    return field('Queixa principal', ta('queixaPrincipal', 'Descreva em poucas palavras o que te trouxe à terapia', 3)) +
      field('Motivo da consulta — detalhes <span style="color:#9ca3af">(opcional)</span>', ta('motivoConsulta', 'Quando começou? Como afeta seu dia a dia?', 4));
  }

  function renderStep3() {
    return '<div class="note">Essas informações ajudam seu terapeuta a oferecer o melhor atendimento. Todos os dados são confidenciais.</div>' +
      field('Doenças prévias', ta('doencasPrevias', 'Ex: diabetes, hipertensão, cirurgias (deixe em branco se não houver)', 2)) +
      field('Medicações em uso', ta('medicacoes', 'Nome do medicamento e para que usa (ex: Rivotril para ansiedade)', 2)) +
      field('Alergias', ta('alergias', 'Ex: alergia a dipirona, penicilina, frutos do mar', 2)) +
      field('Antecedentes familiares', ta('antecedentesFamiliares', 'Histórico de problemas de saúde mental na família', 2));
  }

  function renderStep4() {
    return field('Alimentação', ta('alimentacao', 'Como você descreve sua alimentação?', 2)) +
      field('Atividade física', ta('atividadeFisica', 'Frequência e tipo de atividade', 2)) +
      field('Tabagismo', sel('tabagismo', [{v:'',l:'Selecione...'},{v:'Não',l:'Não fumo'},{v:'Ex-fumante',l:'Ex-fumante'},{v:'Sim',l:'Sim, fumo'}])) +
      field('Consumo de álcool', sel('alcool', [{v:'',l:'Selecione...'},{v:'Não',l:'Não consumo'},{v:'Ocasional',l:'Ocasional (fins de semana)'},{v:'Frequente',l:'Frequente'}]));
  }

  function renderStep5() {
    return '<div class="note gray">Se não tiver feito nenhum exame relevante, pode pular esta etapa.</div>' +
      field('Exames já realizados', ta('examesRealizados', 'Liste exames relevantes (ex: hemograma, neurológico)', 3)) +
      field('Resultados de exames', ta('resultadosExames', 'Resumo dos resultados mais recentes', 3));
  }

  function renderStep6() {
    var checked = state.consentGiven;
    var btn = '<button class="btn-submit ' + (checked ? 'enabled' : 'disabled') + '" id="btn-submit"' + (checked ? '' : ' disabled') + '>' +
      (state.loading ? 'Enviando...' : '✓ Enviar Formulário') + '</button>';
    var hint = checked ? '' : '<div class="hint">Aceite o termo acima para enviar</div>';
    return '<div style="text-align:center;padding:8px 0 16px"><span style="font-size:48px">🔐</span>' +
      '<h2 style="font-size:18px;font-weight:700;margin-top:8px">Seus dados estão protegidos</h2>' +
      '<p style="font-size:13px;color:#6b7280;margin-top:4px">Antes de enviar, leia e aceite o termo abaixo.</p></div>' +
      '<label class="consent-label">' +
        '<input type="checkbox" id="consent-check"' + (checked ? ' checked' : '') + ' />' +
        '<span class="consent-text">Li e concordo com o tratamento dos meus dados de saúde para fins de atendimento terapêutico, conforme a <strong>LGPD (Lei 13.709/2018)</strong>. Meus dados serão usados exclusivamente pelo meu terapeuta.</span>' +
      '</label>' +
      btn + hint;
  }

  /* ── Bind events ── */
  function bindForm() {
    // Data inputs
    app.querySelectorAll('[data-key]').forEach(function (el) {
      var key = el.dataset.key;
      var ev = (el.tagName === 'SELECT' || el.type === 'checkbox' || el.type === 'radio') ? 'change' : 'input';
      el.addEventListener(ev, function () {
        state[key] = (el.type === 'checkbox') ? el.checked : el.value;
        saveDraft();
        if (key === 'birthDate' || key === 'name') rerender();
      });
    });

    // Consent checkbox (renders in step 6 with id)
    var cc = document.getElementById('consent-check');
    if (cc) {
      cc.addEventListener('change', function () {
        state.consentGiven = cc.checked;
        rerender();
      });
    }

    var btnNext = document.getElementById('btn-next');
    var btnPrev = document.getElementById('btn-prev');
    var btnPrev2 = document.getElementById('btn-prev2');
    var btnSubmit = document.getElementById('btn-submit');

    if (btnNext) btnNext.addEventListener('click', nextStep);
    if (btnPrev) btnPrev.addEventListener('click', prevStep);
    if (btnPrev2) btnPrev2.addEventListener('click', prevStep);
    if (btnSubmit) btnSubmit.addEventListener('click', handleSubmit);
  }

  function rerender() {
    var scrollY = window.scrollY;
    render();
    window.scrollTo(0, scrollY);
  }

  function nextStep() {
    if (state.step === 1 && !state.name.trim()) {
      state.nameError = 'Nome completo é obrigatório';
      rerender();
      return;
    }
    state.nameError = '';
    state.step = Math.min(state.step + 1, STEPS.length);
    window.scrollTo(0, 0);
    rerender();
  }

  function prevStep() {
    state.step = Math.max(state.step - 1, 1);
    window.scrollTo(0, 0);
    rerender();
  }

  async function handleSubmit() {
    if (!state.consentGiven || state.loading) return;
    state.loading = true;
    rerender();

    var items = [
      { key: 'queixa_principal', label: 'Queixa Principal', value: state.queixaPrincipal },
      { key: 'motivo_consulta', label: 'Motivo da Consulta', value: state.motivoConsulta },
      { key: 'doencas_previas', label: 'Doenças Prévias', value: state.doencasPrevias },
      { key: 'medicacoes', label: 'Medicações em Uso', value: state.medicacoes },
      { key: 'alergias', label: 'Alergias', value: state.alergias },
      { key: 'antecedentes_familiares', label: 'Antecedentes Familiares', value: state.antecedentesFamiliares },
      { key: 'alimentacao', label: 'Alimentação', value: state.alimentacao },
      { key: 'atividade_fisica', label: 'Atividade Física', value: state.atividadeFisica },
      { key: 'tabagismo', label: 'Tabagismo', value: state.tabagismo },
      { key: 'alcool', label: 'Consumo de Álcool', value: state.alcool },
      { key: 'exames_realizados', label: 'Exames Realizados', value: state.examesRealizados },
      { key: 'resultados_exames', label: 'Resultados de Exames', value: state.resultadosExames },
    ].filter(function (i) { return i.value && i.value.trim(); });

    var body = { name: state.name.trim(), consentGiven: true };
    if (state.birthDate) body.birthDate = state.birthDate;
    if (state.gender) body.gender = state.gender;
    if (state.profession) body.profession = state.profession;
    if (state.email) body.email = state.email;
    if (state.phone) body.phone = state.phone;
    if (items.length > 0) body.anamnesis = { items: items, updatedAt: new Date().toISOString() };

    try {
      var res = await fetch('/api/patients/intake?token=' + encodeURIComponent(TOKEN), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });

      if (res.ok) {
        try { localStorage.removeItem(DRAFT_KEY + '_' + TOKEN); } catch (e) {}
        var firstName = state.name.trim().split(' ')[0];
        renderSuccess(firstName);
      } else {
        var data = {};
        try { data = await res.json(); } catch (e) {}
        if (res.status === 401 || (data.message && String(data.message).includes('inválido'))) {
          renderError('invalid');
        } else if (data.message === 'TOKEN_EXPIRED') {
          renderError('expired');
        } else {
          alert(data.message || 'Erro ao enviar. Tente novamente.');
          state.loading = false;
          rerender();
        }
      }
    } catch (e) {
      alert('Erro de conexão. Verifique sua internet e tente novamente.');
      state.loading = false;
      rerender();
    }
  }

  /* ── Init ── */
  loadDraft();
  render();
})();
