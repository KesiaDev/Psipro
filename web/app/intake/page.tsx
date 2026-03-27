'use client';

import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'next/navigation';

const API_BASE = (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3001/api').replace(/\/+$/, '');
const DRAFT_KEY = 'psipro_intake_draft';

interface AnamnesisItem {
  key: string;
  label: string;
  value: string;
}

interface FormData {
  name: string;
  birthDate: string;
  gender: string;
  profession: string;
  email: string;
  phone: string;
  queixaPrincipal: string;
  motivoConsulta: string;
  doencasPrevias: string;
  medicacoes: string;
  alergias: string;
  antecedentesFamiliares: string;
  alimentacao: string;
  atividadeFisica: string;
  tabagismo: string;
  alcool: string;
  examesRealizados: string;
  resultadosExames: string;
  consentGiven: boolean;
}

const INITIAL_FORM: FormData = {
  name: '',
  birthDate: '',
  gender: '',
  profession: '',
  email: '',
  phone: '',
  queixaPrincipal: '',
  motivoConsulta: '',
  doencasPrevias: '',
  medicacoes: '',
  alergias: '',
  antecedentesFamiliares: '',
  alimentacao: '',
  atividadeFisica: '',
  tabagismo: '',
  alcool: '',
  examesRealizados: '',
  resultadosExames: '',
  consentGiven: false,
};

const STEPS = [
  { id: 1, icon: '👤', title: 'Identificação', subtitle: 'Seus dados pessoais' },
  { id: 2, icon: '💬', title: 'Queixa Principal', subtitle: 'O que te trouxe à terapia?' },
  { id: 3, icon: '🏥', title: 'Histórico de Saúde', subtitle: 'Informações de saúde' },
  { id: 4, icon: '🌿', title: 'Hábitos de Vida', subtitle: 'Seu estilo de vida' },
  { id: 5, icon: '🔬', title: 'Exames', subtitle: 'Exames complementares' },
  { id: 6, icon: '🔐', title: 'Consentimento', subtitle: 'Privacidade e LGPD' },
];

function calcIdade(birthDate: string): string {
  if (!birthDate) return '';
  const birth = new Date(birthDate);
  const today = new Date();
  const age = today.getFullYear() - birth.getFullYear() -
    (today < new Date(today.getFullYear(), birth.getMonth(), birth.getDate()) ? 1 : 0);
  return isNaN(age) || age < 0 || age > 150 ? '' : `${age} anos`;
}

export default function IntakePage() {
  const searchParams = useSearchParams();
  const token = searchParams.get('token');

  const [step, setStep] = useState(1);
  const [form, setForm] = useState<FormData>(INITIAL_FORM);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<'form' | 'success' | 'expired' | 'invalid'>('form');
  const [nameError, setNameError] = useState('');

  // Carregar rascunho do localStorage
  useEffect(() => {
    if (!token) return;
    try {
      const draft = localStorage.getItem(`${DRAFT_KEY}_${token}`);
      if (draft) {
        const parsed = JSON.parse(draft);
        setForm({ ...INITIAL_FORM, ...parsed, consentGiven: false });
      }
    } catch {
      // ignora erro de parse
    }
  }, [token]);

  // Salvar rascunho automaticamente
  const saveDraft = useCallback((data: FormData) => {
    if (!token) return;
    try {
      localStorage.setItem(`${DRAFT_KEY}_${token}`, JSON.stringify(data));
    } catch {
      // ignora
    }
  }, [token]);

  if (!token) {
    return <ErrorScreen type="invalid" />;
  }

  function updateField(field: keyof FormData, value: string | boolean) {
    const updated = { ...form, [field]: value };
    setForm(updated);
    saveDraft(updated);
  }

  function nextStep() {
    if (step === 1 && !form.name.trim()) {
      setNameError('Nome completo é obrigatório');
      return;
    }
    setNameError('');
    setStep((s) => Math.min(s + 1, STEPS.length));
  }

  function prevStep() {
    setStep((s) => Math.max(s - 1, 1));
  }

  async function handleSubmit() {
    if (!form.consentGiven) return;
    setLoading(true);

    const anamnesisItems: AnamnesisItem[] = [
      { key: 'queixa_principal', label: 'Queixa Principal', value: form.queixaPrincipal },
      { key: 'motivo_consulta', label: 'Motivo da Consulta', value: form.motivoConsulta },
      { key: 'doencas_previas', label: 'Doenças Prévias', value: form.doencasPrevias },
      { key: 'medicacoes', label: 'Medicações em Uso', value: form.medicacoes },
      { key: 'alergias', label: 'Alergias', value: form.alergias },
      { key: 'antecedentes_familiares', label: 'Antecedentes Familiares', value: form.antecedentesFamiliares },
      { key: 'alimentacao', label: 'Alimentação', value: form.alimentacao },
      { key: 'atividade_fisica', label: 'Atividade Física', value: form.atividadeFisica },
      { key: 'tabagismo', label: 'Tabagismo', value: form.tabagismo },
      { key: 'alcool', label: 'Consumo de Álcool', value: form.alcool },
      { key: 'exames_realizados', label: 'Exames Realizados', value: form.examesRealizados },
      { key: 'resultados_exames', label: 'Resultados de Exames', value: form.resultadosExames },
    ].filter((item) => item.value.trim() !== '');

    const body: Record<string, unknown> = {
      name: form.name.trim(),
      consentGiven: true,
    };
    if (form.birthDate) body.birthDate = form.birthDate;
    if (form.gender) body.gender = form.gender;
    if (form.profession) body.profession = form.profession;
    if (form.email) body.email = form.email;
    if (form.phone) body.phone = form.phone;
    if (anamnesisItems.length > 0) {
      body.anamnesis = { items: anamnesisItems, updatedAt: new Date().toISOString() };
    }

    try {
      const res = await fetch(`${API_BASE}/patients/intake?token=${token}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });

      if (res.ok) {
        localStorage.removeItem(`${DRAFT_KEY}_${token}`);
        setStatus('success');
      } else {
        const data = await res.json().catch(() => ({}));
        if (res.status === 401 || (data?.message && String(data.message).includes('inválido'))) {
          setStatus('invalid');
        } else if (data?.message === 'TOKEN_EXPIRED') {
          setStatus('expired');
        } else {
          alert(data?.message || 'Erro ao enviar. Tente novamente.');
        }
      }
    } catch {
      alert('Erro de conexão. Verifique sua internet e tente novamente.');
    } finally {
      setLoading(false);
    }
  }

  if (status === 'success') return <SuccessScreen name={form.name} />;
  if (status === 'expired') return <ErrorScreen type="expired" />;
  if (status === 'invalid') return <ErrorScreen type="invalid" />;

  const progress = Math.round((step / STEPS.length) * 100);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-lg mx-auto px-4 py-3">
          <div className="flex items-center justify-between mb-3">
            <span className="text-lg font-bold text-indigo-600">PsiPro</span>
            <span className="text-xs text-gray-500 flex items-center gap-1">
              🔒 Formulário Seguro
            </span>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex-1 bg-gray-200 rounded-full h-2">
              <div
                className="bg-indigo-500 h-2 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
            <span className="text-xs text-gray-500 whitespace-nowrap">
              {step}/{STEPS.length}
            </span>
          </div>
        </div>
      </header>

      {/* Main */}
      <main className="flex-1 max-w-lg mx-auto w-full px-4 py-6">
        {/* Step indicator */}
        <div className="mb-6 text-center">
          <span className="text-3xl">{STEPS[step - 1].icon}</span>
          <h1 className="text-xl font-bold text-gray-800 mt-1">{STEPS[step - 1].title}</h1>
          <p className="text-sm text-gray-500">{STEPS[step - 1].subtitle}</p>
        </div>

        {/* Step content */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
          {step === 1 && (
            <StepIdentificacao
              form={form}
              update={updateField}
              nameError={nameError}
            />
          )}
          {step === 2 && <StepQueixa form={form} update={updateField} />}
          {step === 3 && <StepSaude form={form} update={updateField} />}
          {step === 4 && <StepHabitos form={form} update={updateField} />}
          {step === 5 && <StepExames form={form} update={updateField} />}
          {step === 6 && (
            <StepConsentimento
              form={form}
              update={updateField}
              onSubmit={handleSubmit}
              loading={loading}
            />
          )}
        </div>

        {/* Navigation */}
        {step < 6 && (
          <div className="flex gap-3 mt-4">
            {step > 1 && (
              <button
                onClick={prevStep}
                className="flex-1 py-3 border border-gray-300 text-gray-700 rounded-xl font-medium hover:bg-gray-50 transition-colors"
              >
                ← Anterior
              </button>
            )}
            <button
              onClick={nextStep}
              className="flex-1 py-3 bg-indigo-600 text-white rounded-xl font-medium hover:bg-indigo-700 transition-colors"
            >
              {step === 5 ? 'Finalizar →' : 'Próximo →'}
            </button>
          </div>
        )}
        {step > 1 && step === 6 && (
          <button
            onClick={prevStep}
            className="w-full mt-3 py-2 text-sm text-gray-500 hover:text-gray-700 transition-colors"
          >
            ← Voltar
          </button>
        )}
      </main>
    </div>
  );
}

// ─── Etapas ───────────────────────────────────────────────────────────────────

function StepIdentificacao({
  form,
  update,
  nameError,
}: {
  form: FormData;
  update: (f: keyof FormData, v: string | boolean) => void;
  nameError: string;
}) {
  const idade = calcIdade(form.birthDate);
  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Nome completo <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          autoFocus
          value={form.name}
          onChange={(e) => update('name', e.target.value)}
          placeholder="Ex: Maria da Silva"
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
        />
        {nameError && <p className="text-red-500 text-xs mt-1">{nameError}</p>}
      </div>

      <div className="flex gap-3">
        <div className="flex-1">
          <label className="block text-sm font-medium text-gray-700 mb-1">Data de nascimento</label>
          <input
            type="date"
            value={form.birthDate}
            onChange={(e) => update('birthDate', e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>
        {idade && (
          <div className="w-24 flex flex-col justify-end">
            <label className="block text-sm font-medium text-gray-700 mb-1">Idade</label>
            <div className="border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-600 bg-gray-50">
              {idade}
            </div>
          </div>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Sexo</label>
        <div className="flex gap-3 flex-wrap">
          {['Masculino', 'Feminino', 'Outro'].map((opt) => (
            <label key={opt} className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="gender"
                value={opt}
                checked={form.gender === opt}
                onChange={() => update('gender', opt)}
                className="accent-indigo-600"
              />
              <span className="text-sm text-gray-700">{opt}</span>
            </label>
          ))}
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Profissão</label>
        <input
          type="text"
          value={form.profession}
          onChange={(e) => update('profession', e.target.value)}
          placeholder="Ex: Professora"
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
        />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">E-mail</label>
          <input
            type="email"
            value={form.email}
            onChange={(e) => update('email', e.target.value)}
            placeholder="seu@email.com"
            className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Telefone</label>
          <input
            type="tel"
            inputMode="tel"
            value={form.phone}
            onChange={(e) => update('phone', e.target.value)}
            placeholder="(11) 99999-0000"
            className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>
      </div>
    </div>
  );
}

function StepQueixa({ form, update }: { form: FormData; update: (f: keyof FormData, v: string | boolean) => void }) {
  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Queixa principal</label>
        <textarea
          autoFocus
          value={form.queixaPrincipal}
          onChange={(e) => update('queixaPrincipal', e.target.value)}
          placeholder="Descreva em poucas palavras o que te trouxe à terapia"
          rows={3}
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Motivo da consulta — detalhes <span className="text-gray-400">(opcional)</span>
        </label>
        <textarea
          value={form.motivoConsulta}
          onChange={(e) => update('motivoConsulta', e.target.value)}
          placeholder="Quando começou? Como afeta seu dia a dia?"
          rows={4}
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
        />
      </div>
    </div>
  );
}

function StepSaude({ form, update }: { form: FormData; update: (f: keyof FormData, v: string | boolean) => void }) {
  return (
    <div className="space-y-4">
      <p className="text-xs text-gray-500 bg-indigo-50 rounded-lg px-3 py-2">
        Essas informações ajudam seu terapeuta a oferecer o melhor atendimento. Todos os dados são confidenciais.
      </p>
      {[
        { field: 'doencasPrevias' as keyof FormData, label: 'Doenças prévias', placeholder: 'Ex: diabetes, hipertensão, cirurgias (deixe em branco se não houver)' },
        { field: 'medicacoes' as keyof FormData, label: 'Medicações em uso', placeholder: 'Nome do medicamento e para que usa (ex: Rivotril para ansiedade)' },
        { field: 'alergias' as keyof FormData, label: 'Alergias', placeholder: 'Ex: alergia a dipirona, penicilina, frutos do mar' },
        { field: 'antecedentesFamiliares' as keyof FormData, label: 'Antecedentes familiares', placeholder: 'Histórico de problemas de saúde mental na família (deixe em branco se não souber)' },
      ].map(({ field, label, placeholder }) => (
        <div key={field}>
          <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
          <textarea
            value={form[field] as string}
            onChange={(e) => update(field, e.target.value)}
            placeholder={placeholder}
            rows={2}
            className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
          />
        </div>
      ))}
    </div>
  );
}

function StepHabitos({ form, update }: { form: FormData; update: (f: keyof FormData, v: string | boolean) => void }) {
  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Alimentação</label>
        <textarea
          value={form.alimentacao}
          onChange={(e) => update('alimentacao', e.target.value)}
          placeholder="Como você descreve sua alimentação?"
          rows={2}
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Atividade física</label>
        <textarea
          value={form.atividadeFisica}
          onChange={(e) => update('atividadeFisica', e.target.value)}
          placeholder="Frequência e tipo de atividade"
          rows={2}
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Tabagismo</label>
        <select
          value={form.tabagismo}
          onChange={(e) => update('tabagismo', e.target.value)}
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
        >
          <option value="">Selecione...</option>
          <option value="Não">Não fumo</option>
          <option value="Ex-fumante">Ex-fumante</option>
          <option value="Sim">Sim, fumo</option>
        </select>
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Consumo de álcool</label>
        <select
          value={form.alcool}
          onChange={(e) => update('alcool', e.target.value)}
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
        >
          <option value="">Selecione...</option>
          <option value="Não">Não consumo</option>
          <option value="Ocasional">Ocasional (fins de semana)</option>
          <option value="Frequente">Frequente</option>
        </select>
      </div>
    </div>
  );
}

function StepExames({ form, update }: { form: FormData; update: (f: keyof FormData, v: string | boolean) => void }) {
  return (
    <div className="space-y-4">
      <p className="text-xs text-gray-500 bg-gray-50 rounded-lg px-3 py-2">
        Se não tiver feito nenhum exame relevante, pode pular esta etapa.
      </p>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Exames já realizados</label>
        <textarea
          value={form.examesRealizados}
          onChange={(e) => update('examesRealizados', e.target.value)}
          placeholder="Liste exames relevantes (ex: hemograma, neurológico)"
          rows={3}
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Resultados de exames</label>
        <textarea
          value={form.resultadosExames}
          onChange={(e) => update('resultadosExames', e.target.value)}
          placeholder="Resumo dos resultados mais recentes"
          rows={3}
          className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
        />
      </div>
    </div>
  );
}

function StepConsentimento({
  form,
  update,
  onSubmit,
  loading,
}: {
  form: FormData;
  update: (f: keyof FormData, v: string | boolean) => void;
  onSubmit: () => void;
  loading: boolean;
}) {
  return (
    <div className="space-y-5">
      <div className="text-center py-2">
        <span className="text-4xl">🔐</span>
        <h2 className="text-lg font-bold text-gray-800 mt-2">Seus dados estão protegidos</h2>
        <p className="text-sm text-gray-500 mt-1">
          Antes de enviar, leia e aceite o termo abaixo.
        </p>
      </div>

      <label className="flex items-start gap-3 cursor-pointer p-4 border border-gray-200 rounded-xl hover:bg-gray-50 transition-colors">
        <input
          type="checkbox"
          checked={form.consentGiven}
          onChange={(e) => update('consentGiven', e.target.checked)}
          className="mt-0.5 w-5 h-5 accent-indigo-600 flex-shrink-0"
        />
        <span className="text-sm text-gray-700 leading-relaxed">
          Li e concordo com o tratamento dos meus dados de saúde para fins de atendimento
          terapêutico, conforme a{' '}
          <strong>LGPD (Lei 13.709/2018)</strong>. Meus dados serão usados exclusivamente
          pelo meu terapeuta.
        </span>
      </label>

      <button
        onClick={onSubmit}
        disabled={!form.consentGiven || loading}
        className={`w-full py-4 rounded-xl font-bold text-white text-base transition-all ${
          form.consentGiven && !loading
            ? 'bg-green-500 hover:bg-green-600 active:scale-95'
            : 'bg-gray-300 cursor-not-allowed'
        }`}
      >
        {loading ? 'Enviando...' : '✓ Enviar Formulário'}
      </button>

      {!form.consentGiven && (
        <p className="text-center text-xs text-gray-400">
          Aceite o termo acima para enviar
        </p>
      )}
    </div>
  );
}

// ─── Telas de resultado ────────────────────────────────────────────────────────

function SuccessScreen({ name }: { name: string }) {
  const firstName = name.split(' ')[0];
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center">
        <div className="text-6xl mb-4">✅</div>
        <h1 className="text-2xl font-bold text-gray-800 mb-2">Dados enviados com sucesso!</h1>
        <p className="text-gray-600 mb-4">
          Obrigado, <strong>{firstName}</strong>! Seu terapeuta já foi notificado e
          entrará em contato em breve.
        </p>
        <div className="text-xs text-gray-400 border-t border-gray-100 pt-4 mt-4">
          Suas informações estão protegidas pela Lei Geral de Proteção de Dados (LGPD).
        </div>
      </div>
    </div>
  );
}

function ErrorScreen({ type }: { type: 'expired' | 'invalid' }) {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center">
        <div className="text-5xl mb-4">{type === 'expired' ? '⚠️' : '❌'}</div>
        <h1 className="text-xl font-bold text-gray-800 mb-2">
          {type === 'expired' ? 'Este link expirou' : 'Link inválido'}
        </h1>
        <p className="text-gray-600">
          {type === 'expired'
            ? 'O link de cadastro é válido por 7 dias. Solicite um novo link ao seu terapeuta.'
            : 'Este link não é válido ou já foi utilizado. Solicite um novo link ao seu terapeuta.'}
        </p>
      </div>
    </div>
  );
}
