import { Suspense } from "react";
import HandoffClient from "./HandoffClient";

export default function HandoffPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-psipro-background">
          <div className="text-psipro-text-secondary">Conectando sua sessão...</div>
        </div>
      }
    >
      <HandoffClient />
    </Suspense>
  );
}

