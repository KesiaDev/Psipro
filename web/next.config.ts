import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async redirects() {
    return [
      { source: "/clinics", destination: "/clinica", permanent: false },
    ];
  },
};

export default nextConfig;
