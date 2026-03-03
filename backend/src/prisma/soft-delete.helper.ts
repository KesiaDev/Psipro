/**
 * Filtros para excluir registros soft-deleted.
 * Use em todas as queries de Patient, Appointment, Session, Payment.
 */
export const NOT_DELETED = {
  patient: { deletedAt: null },
  appointment: { deletedAt: null },
  session: { deletedAt: null },
  payment: { deletedAt: null },
} as const;

/** Mescla where com filtro de não-deletado */
export function whereNotDeleted<T extends keyof typeof NOT_DELETED>(
  model: T,
  where: Record<string, unknown> = {},
): Record<string, unknown> {
  return { ...where, ...NOT_DELETED[model] };
}
