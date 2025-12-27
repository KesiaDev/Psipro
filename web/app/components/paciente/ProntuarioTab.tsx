"use client";

import { useState } from "react";

export default function ProntuarioTab() {
  const [searchTerm, setSearchTerm] = useState("");
  const [dateFilter, setDateFilter] = useState("all");

  const sessions = [
    {
      id: 1,
      date: "2025-03-12",
      dateFormatted: "12/03/2025",
      title: "Sessão 12 - Técnicas de relaxamento",
      content:
        "Trabalhamos técnicas de respiração e relaxamento progressivo. Paciente demonstrou interesse e comprometimento. Próximos passos: prática diária das técnicas.",
    },
    {
      id: 2,
      date: "2025-03-05",
      dateFormatted: "05/03/2025",
      title: "Sessão 11 - Análise de padrões",
      content:
        "Exploramos padrões de pensamento relacionados à ansiedade. Identificamos gatilhos principais relacionados ao trabalho e responsabilidades familiares.",
    },
    {
      id: 3,
      date: "2025-02-26",
      dateFormatted: "26/02/2025",
      title: "Sessão 10 - Estabelecimento de metas",
      content:
        "Definimos metas claras para os próximos meses: melhorar qualidade do sono, reduzir episódios de ansiedade e estabelecer limites saudáveis no trabalho.",
    },
  ];

  const filteredSessions = sessions.filter((session) => {
    const matchesSearch =
      searchTerm === "" ||
      session.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      session.content.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesDate =
      dateFilter === "all" ||
      (dateFilter === "thisMonth" && session.date.startsWith("2025-03")) ||
      (dateFilter === "lastMonth" && session.date.startsWith("2025-02")) ||
      (dateFilter === "thisYear" && session.date.startsWith("2025"));

    return matchesSearch && matchesDate;
  });

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-psipro-text mb-1">Prontuário / Histórico Clínico</h2>
        <p className="text-sm text-psipro-text-secondary">
          Lista cronológica de sessões e anotações clínicas
        </p>
      </div>

      {/* Filtros e busca */}
      <div className="mb-6 space-y-4">
        <div className="relative">
          <input
            type="text"
            placeholder="Buscar por palavra-chave no histórico..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full px-4 py-3 pl-11 border border-psipro-border rounded-lg focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm shadow-sm bg-psipro-surface-elevated text-psipro-text"
          />
          <span className="absolute left-3.5 top-1/2 transform -translate-y-1/2 text-psipro-text-muted text-lg">
            🔍
          </span>
        </div>
        <div className="flex items-center gap-3">
          <label className="text-sm font-medium text-psipro-text-secondary">Filtrar por:</label>
          <select
            value={dateFilter}
            onChange={(e) => setDateFilter(e.target.value)}
            className="px-3 py-2 border border-psipro-border rounded-lg bg-psipro-surface-elevated text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm"
          >
            <option value="all">Todas as datas</option>
            <option value="thisMonth">Este mês</option>
            <option value="lastMonth">Mês passado</option>
            <option value="thisYear">Este ano</option>
          </select>
          {filteredSessions.length !== sessions.length && (
            <span className="text-sm text-psipro-text-secondary">
              {filteredSessions.length} de {sessions.length} registros
            </span>
          )}
        </div>
      </div>

      {/* Lista de sessões */}
      {filteredSessions.length > 0 ? (
        <div className="space-y-4">
          {filteredSessions.map((session) => (
            <div
              key={session.id}
              className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 hover:shadow-md transition-all duration-200"
            >
              <div className="flex items-start justify-between mb-3">
                <h3 className="text-base font-semibold text-psipro-text">
                  {session.title}
                </h3>
                <span className="text-sm text-psipro-text-muted whitespace-nowrap ml-4 font-medium">
                  {session.dateFormatted}
                </span>
              </div>
              <p className="text-psipro-text-secondary leading-relaxed text-base">{session.content}</p>
            </div>
          ))}
        </div>
      ) : sessions.length === 0 ? (
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-12 text-center">
          <div className="text-5xl mb-4 opacity-60">📝</div>
          <p className="text-psipro-text-secondary text-base mb-2 font-medium">
            Ainda não há informações registradas para este paciente.
          </p>
          <p className="text-psipro-text-muted text-sm">
            As anotações das sessões aparecerão aqui conforme você for registrando.
          </p>
        </div>
      ) : (
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-12 text-center">
          <div className="text-5xl mb-4 opacity-60">🔍</div>
          <p className="text-psipro-text-secondary text-base mb-2 font-medium">
            Nenhum registro encontrado com os filtros aplicados.
          </p>
          <p className="text-psipro-text-muted text-sm">
            Tente ajustar os filtros de busca ou data.
          </p>
        </div>
      )}
    </div>
  );
}

