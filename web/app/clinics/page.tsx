import { redirect } from "next/navigation";

/**
 * Rota /clinics redireciona para /clinica (página de Clínicas).
 * Mantém compatibilidade com quem acessa /clinics.
 */
export default function ClinicsPage() {
  redirect("/clinica");
}
