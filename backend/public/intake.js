/* PsiPro — Formulário de Intake · vanilla JS */
(function () {
  'use strict';

  var DRAFT_KEY = 'psipro_intake_draft';
  var STEPS = [
    { id: 1, icon: '👤', title: 'Identificação',      sub: 'Seus dados pessoais' },
    { id: 2, icon: '💬', title: 'Queixa Principal',   sub: 'O que te trouxe à terapia?' },
    { id: 3, icon: '🏥', title: 'Histórico de Saúde', sub: 'Informações de saúde' },
    { id: 4, icon: '🌿', title: 'Hábitos de Vida',    sub: 'Seu estilo de vida' },
    { id: 5, icon: '🔬', title: 'Exames',             sub: 'Exames complementares' },
    { id: 6, icon: '🔐', title: 'Consentimento',      sub: 'Privacidade e LGPD' },
  ];

  var params = new URLSearchParams(window.location.search);
  var TOKEN = params.get('token');

  var state = {
    step: 1, loading: false, nameError: '',
    name: '', birthDate: '', gender: '', profession: '',
    email: '', phone: '',
    queixaPrincipal: '', motivoConsulta: '',
    doencasPrevias: '', medicacoes: '', alergias: '', antecedentesFamiliares: '',
    alimentacao: '', atividadeFisica: '', tabagismo: '', alcool: '',
    examesRealizados: '', resultadosExames: '',
    consentGiven: false,
  };

  /* ── Draft ─────────────────────────────────────────── */
  function loadDraft() {
    if (!TOKEN) return;
    try {
      var raw = localStorage.getItem(DRAFT_KEY + '_' + TOKEN);
      if (raw) {
        var d = JSON.parse(raw);
        Object.keys(d).forEach(function (k) {
          if (k !== 'consentGiven' && k !== 'loading' && k !== 'nameError' && k !== 'step') {
            state[k] = d[k];
          }
        });
      }
    } catch (_) {}
  }
  function saveDraft() {
    if (!TOKEN) return;
    try { localStorage.setItem(DRAFT_KEY + '_' + TOKEN, JSON.stringify(state)); } catch (_) {}
  }

  /* ── Render helpers ─────────────────────────────────── */
  var app = document.getElementById('app');

  function esc(s) {
    return String(s == null ? '' : s)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;')
      .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  function calcAge(bd) {
    if (!bd) return '';
    var b = new Date(bd), t = new Date();
    var age = t.getFullYear() - b.getFullYear() -
      (t < new Date(t.getFullYear(), b.getMonth(), b.getDate()) ? 1 : 0);
    return (isNaN(age) || age < 0 || age > 150) ? '' : age + ' anos';
  }

  /* ── Main render ─────────────────────────────────────── */
  function render() {
    if (!TOKEN) { showError('invalid'); return; }
    var s = state.step;
    var total = STEPS.length;
    var pct = Math.round((s / total) * 100);
    var info = STEPS[s - 1];

    var dots = STEPS.map(function (step, i) {
      var cls = i + 1 < s ? 'done' : i + 1 === s ? 'active' : '';
      return '<div class="step-dot ' + cls + '"></div>';
    }).join('');

    app.innerHTML =
      '<header>' +
        '<div class="header-inner">' +
          '<div class="header-top">' +
            '<span class="logo">PsiPro</span>' +
            '<span class="secure-badge">🔒&nbsp;Formulário Seguro</span>' +
          '</div>' +
          '<div class="progress-wrap">' +
            '<div class="progress-bar"><div class="progress-fill" style="width:' + pct + '%"></div></div>' +
            '<span class="progress-label">' + s + '&thinsp;/&thinsp;' + total + '</span>' +
          '</div>' +
        '</div>' +
      '</header>' +
      '<main>' +
        '<div class="step-dots">' + dots + '</div>' +
        '<div class="step-hd">' +
          '<div class="step-icon">' + info.icon + '</div>' +
          '<div class="step-title">' + esc(info.title) + '</div>' +
          '<div class="step-sub">' + esc(info.sub) + '</div>' +
        '</div>' +
        '<div class="card">' + buildStep(s) + '</div>' +
        buildNav(s) +
      '</main>';

    bindEvents();
  }

  function showError(type) {
    var icon  = type === 'expired' ? '⚠️' : '❌';
    var title = type === 'expired' ? 'Este link expirou' : 'Link inválido';
    var body  = type === 'expired'
      ? 'O link de cadastro é válido por 7 dias. Solicite um novo link ao seu terapeuta.'
      : 'Este link não é válido ou já foi utilizado. Solicite um novo link ao seu terapeuta.';
    app.innerHTML =
      '<div class="screen"><div class="screen-card">' +
        '<div class="screen-icon">' + icon + '</div>' +
        '<div class="screen-title">' + title + '</div>' +
        '<p class="screen-body">' + body + '</p>' +
      '</div></div>';
  }

  function showSuccess(firstName) {
    app.innerHTML =
      '<div class="screen"><div class="screen-card">' +
        '<div class="screen-icon">✅</div>' +
        '<div class="screen-title">Dados enviados com sucesso!</div>' +
        '<p class="screen-body">Obrigado, <strong>' + esc(firstName) + '</strong>!<br>Seu terapeuta já foi notificado e entrará em contato em breve.</p>' +
        '<div class="screen-foot">Suas informações estão protegidas pela<br>Lei Geral de Proteção de Dados (LGPD).</div>' +
      '</div></div>';
  }

  /* ── Nav buttons ──────────────────────────────────────── */
  function buildNav(s) {
    var html = '<div class="nav">';
    if (s > 1) html += '<button class="btn btn-secondary" id="btn-prev">← Voltar</button>';
    if (s < STEPS.length) {
      html += '<button class="btn btn-primary" id="btn-next">' + (s === 5 ? 'Próximo →' : 'Próximo →') + '</button>';
    }
    html += '</div>';
    if (s > 1 && s === STEPS.length) {
      html += '<button class="btn-back" id="btn-back">← Voltar à etapa anterior</button>';
    }
    return html;
  }

  /* ── Steps ─────────────────────────────────────────────── */
  function buildStep(s) {
    if (s === 1) return step1();
    if (s === 2) return step2();
    if (s === 3) return step3();
    if (s === 4) return step4();
    if (s === 5) return step5();
    if (s === 6) return step6();
    return '';
  }

  function fl(label, input, required, optionalText, error) {
    return '<div class="field">' +
      '<label class="field-label">' + label +
        (required ? ' <span class="req">*</span>' : '') +
        (optionalText ? ' <span class="optional">(' + optionalText + ')</span>' : '') +
      '</label>' +
      input +
      (error ? '<div class="error-msg">' + esc(error) + '</div>' : '') +
    '</div>';
  }

  function txt(key, placeholder, rows) {
    return '<textarea data-key="' + key + '" rows="' + (rows || 3) + '" placeholder="' + esc(placeholder) + '">' + esc(state[key]) + '</textarea>';
  }

  function inp(type, key, placeholder, extra) {
    return '<input type="' + type + '" data-key="' + key + '" value="' + esc(state[key]) + '" placeholder="' + esc(placeholder) + '" ' + (extra || '') + '/>';
  }

  function sel(key, opts) {
    return '<select data-key="' + key + '">' +
      opts.map(function (o) {
        return '<option value="' + esc(o.v) + '"' + (state[key] === o.v ? ' selected' : '') + '>' + esc(o.l) + '</option>';
      }).join('') +
    '</select>';
  }

  function radioGroup(key, options) {
    return '<div class="radio-group" id="rg-' + key + '">' +
      options.map(function (opt) {
        var sel = state[key] === opt;
        return '<label class="radio-opt' + (sel ? ' selected' : '') + '" data-radio-key="' + key + '" data-radio-val="' + esc(opt) + '">' +
          '<input type="radio" name="' + key + '" value="' + esc(opt) + '"' + (sel ? ' checked' : '') + ' />' +
          '<span class="radio-dot"></span>' + esc(opt) +
        '</label>';
      }).join('') +
    '</div>';
  }

  /* Step 1 — Identificação */
  function step1() {
    var age = calcAge(state.birthDate);
    return fl('Nome completo', inp('text', 'name', 'Ex: Maria da Silva', 'autofocus'), true, '', state.nameError) +
      '<div class="field"><label class="field-label">Data de nascimento</label>' +
        '<div class="row">' +
          '<input type="date" data-key="birthDate" value="' + esc(state.birthDate) + '" />' +
          (age ? '<div class="age-box">' + esc(age) + '</div>' : '') +
        '</div>' +
      '</div>' +
      '<div class="field"><label class="field-label">Sexo</label>' + radioGroup('gender', ['Masculino', 'Feminino', 'Outro']) + '</div>' +
      fl('Profissão', inp('text', 'profession', 'Ex: Professora')) +
      fl('E-mail', inp('email', 'email', 'seu@email.com')) +
      fl('Telefone', inp('tel', 'phone', '(11) 99999-0000'));
  }

  /* Step 2 — Queixa Principal */
  function step2() {
    return fl('Queixa principal', txt('queixaPrincipal', 'Descreva em poucas palavras o que te trouxe à terapia', 3)) +
      fl('Motivo da consulta — detalhes', txt('motivoConsulta', 'Quando começou? Como afeta seu dia a dia?', 4), false, 'opcional');
  }

  /* Step 3 — Histórico de Saúde */
  function step3() {
    return '<div class="note">Essas informações ajudam seu terapeuta a oferecer o melhor atendimento. Todos os dados são confidenciais.</div>' +
      fl('Doenças prévias', txt('doencasPrevias', 'Ex: diabetes, hipertensão, cirurgias (deixe em branco se não houver)', 2)) +
      fl('Medicações em uso', txt('medicacoes', 'Nome do medicamento e para que usa (ex: Rivotril para ansiedade)', 2)) +
      fl('Alergias', txt('alergias', 'Ex: alergia a dipirona, penicilina, frutos do mar', 2)) +
      fl('Antecedentes familiares', txt('antecedentesFamiliares', 'Histórico de saúde mental na família (deixe em branco se não souber)', 2));
  }

  /* Step 4 — Hábitos de Vida */
  function step4() {
    return fl('Alimentação', txt('alimentacao', 'Como você descreve sua alimentação?', 2)) +
      fl('Atividade física', txt('atividadeFisica', 'Frequência e tipo de atividade', 2)) +
      fl('Tabagismo', sel('tabagismo', [
        { v: '', l: 'Selecione...' },
        { v: 'Não', l: 'Não fumo' },
        { v: 'Ex-fumante', l: 'Ex-fumante' },
        { v: 'Sim', l: 'Sim, fumo' },
      ])) +
      fl('Consumo de álcool', sel('alcool', [
        { v: '', l: 'Selecione...' },
        { v: 'Não', l: 'Não consumo' },
        { v: 'Ocasional', l: 'Ocasional (fins de semana)' },
        { v: 'Frequente', l: 'Frequente' },
      ]));
  }

  /* Step 5 — Exames */
  function step5() {
    return '<div class="note gray">Se não tiver feito nenhum exame relevante, pode pular esta etapa.</div>' +
      fl('Exames já realizados', txt('examesRealizados', 'Liste exames relevantes (ex: hemograma, neurológico)', 3)) +
      fl('Resultados de exames', txt('resultadosExames', 'Resumo dos resultados mais recentes', 3));
  }

  /* Step 6 — Consentimento */
  function step6() {
    var checked = state.consentGiven;
    return '<div class="consent-hero">' +
        '<div class="icon">🔐</div>' +
        '<h2>Seus dados estão protegidos</h2>' +
        '<p>Antes de enviar, leia e aceite o termo abaixo.</p>' +
      '</div>' +
      '<label id="consent-label" class="consent-box' + (checked ? ' checked' : '') + '">' +
        '<input type="checkbox" id="consent-check"' + (checked ? ' checked' : '') + ' />' +
        '<div class="check-icon"><span class="check-mark">✓</span></div>' +
        '<span class="consent-text">Li e concordo com o tratamento dos meus dados de saúde para fins de atendimento terapêutico, conforme a <strong>LGPD (Lei 13.709/2018)</strong>. Meus dados serão usados exclusivamente pelo meu terapeuta.</span>' +
      '</label>' +
      '<button class="btn-submit ' + (checked ? 'enabled' : 'disabled') + '" id="btn-submit"' + (checked ? '' : ' disabled') + '>' +
        (state.loading ? 'Enviando...' : '✓&nbsp;&nbsp;Enviar Formulário') +
      '</button>' +
      (!checked ? '<div class="hint">Aceite o termo acima para poder enviar</div>' : '');
  }

  /* ── Event binding ─────────────────────────────────────── */
  function bindEvents() {
    /* Text / date / select inputs */
    app.querySelectorAll('[data-key]').forEach(function (el) {
      var key = el.dataset.key;
      var ev = (el.tagName === 'SELECT') ? 'change' : 'input';
      el.addEventListener(ev, function () {
        state[key] = el.value;
        saveDraft();
        if (key === 'birthDate') rerender();
      });
    });

    /* Pill-style radio buttons */
    app.querySelectorAll('[data-radio-key]').forEach(function (label) {
      label.addEventListener('click', function (e) {
        e.preventDefault();
        var key = label.dataset.radioKey;
        var val = label.dataset.radioVal;
        state[key] = val;
        saveDraft();
        rerender();
      });
    });

    /* Consent checkbox */
    var cc = document.getElementById('consent-check');
    var cl = document.getElementById('consent-label');
    if (cc && cl) {
      cl.addEventListener('click', function (e) {
        e.preventDefault();
        state.consentGiven = !state.consentGiven;
        rerender();
      });
    }

    /* Nav */
    var btnNext   = document.getElementById('btn-next');
    var btnPrev   = document.getElementById('btn-prev');
    var btnBack   = document.getElementById('btn-back');
    var btnSubmit = document.getElementById('btn-submit');

    if (btnNext)   btnNext.addEventListener('click', goNext);
    if (btnPrev)   btnPrev.addEventListener('click', goPrev);
    if (btnBack)   btnBack.addEventListener('click', goPrev);
    if (btnSubmit) btnSubmit.addEventListener('click', submit);
  }

  function rerender() {
    var y = window.scrollY;
    render();
    window.scrollTo(0, y);
  }

  function goNext() {
    if (state.step === 1 && !state.name.trim()) {
      state.nameError = 'Nome completo é obrigatório';
      rerender(); return;
    }
    state.nameError = '';
    state.step = Math.min(state.step + 1, STEPS.length);
    window.scrollTo({ top: 0, behavior: 'smooth' });
    rerender();
  }

  function goPrev() {
    state.step = Math.max(state.step - 1, 1);
    window.scrollTo({ top: 0, behavior: 'smooth' });
    rerender();
  }

  /* ── Submit ─────────────────────────────────────────────── */
  async function submit() {
    if (!state.consentGiven || state.loading) return;
    state.loading = true;
    rerender();

    var rawItems = [
      { key: 'queixa_principal',        label: 'Queixa Principal',        value: state.queixaPrincipal },
      { key: 'motivo_consulta',         label: 'Motivo da Consulta',      value: state.motivoConsulta },
      { key: 'doencas_previas',         label: 'Doenças Prévias',         value: state.doencasPrevias },
      { key: 'medicacoes',              label: 'Medicações em Uso',       value: state.medicacoes },
      { key: 'alergias',               label: 'Alergias',                value: state.alergias },
      { key: 'antecedentes_familiares', label: 'Antecedentes Familiares', value: state.antecedentesFamiliares },
      { key: 'alimentacao',            label: 'Alimentação',             value: state.alimentacao },
      { key: 'atividade_fisica',        label: 'Atividade Física',        value: state.atividadeFisica },
      { key: 'tabagismo',              label: 'Tabagismo',               value: state.tabagismo },
      { key: 'alcool',                 label: 'Consumo de Álcool',       value: state.alcool },
      { key: 'exames_realizados',       label: 'Exames Realizados',       value: state.examesRealizados },
      { key: 'resultados_exames',       label: 'Resultados de Exames',    value: state.resultadosExames },
    ].filter(function (i) { return i.value && i.value.trim(); });

    var body = { name: state.name.trim(), consentGiven: true };
    if (state.birthDate)  body.birthDate  = state.birthDate;
    if (state.gender)     body.gender     = state.gender;
    if (state.profession) body.profession = state.profession;
    if (state.email)      body.email      = state.email;
    if (state.phone)      body.phone      = state.phone;
    if (rawItems.length)  body.anamnesis  = { items: rawItems, updatedAt: new Date().toISOString() };

    try {
      var res = await fetch('/api/patients/intake?token=' + encodeURIComponent(TOKEN), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      if (res.ok) {
        try { localStorage.removeItem(DRAFT_KEY + '_' + TOKEN); } catch (_) {}
        showSuccess(state.name.trim().split(' ')[0]);
      } else {
        var data = {};
        try { data = await res.json(); } catch (_) {}
        if (res.status === 401 || String(data.message || '').includes('inválido')) {
          showError('invalid');
        } else if (data.message === 'TOKEN_EXPIRED') {
          showError('expired');
        } else {
          alert(data.message || 'Erro ao enviar. Tente novamente.');
          state.loading = false;
          rerender();
        }
      }
    } catch (_) {
      alert('Erro de conexão. Verifique sua internet e tente novamente.');
      state.loading = false;
      rerender();
    }
  }

  /* ── Boot ─────────────────────────────────────────────── */
  loadDraft();
  render();
})();
