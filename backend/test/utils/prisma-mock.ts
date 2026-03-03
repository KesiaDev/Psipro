/**
 * Prisma mock factory para testes isolados.
 * Permite configurar retornos por modelo/ação.
 */
export type PrismaMock = {
  user: {
    findUnique: jest.Mock;
    findFirst: jest.Mock;
    update: jest.Mock;
    create: jest.Mock;
  };
  clinicUser: {
    findUnique: jest.Mock;
    findFirst: jest.Mock;
  };
  refreshToken: {
    findUnique: jest.Mock;
    findFirst: jest.Mock;
    create: jest.Mock;
    updateMany: jest.Mock;
    update: jest.Mock;
  };
  patient: {
    findMany: jest.Mock;
    findFirst: jest.Mock;
    create: jest.Mock;
    update: jest.Mock;
    updateMany: jest.Mock;
    deleteMany: jest.Mock;
  };
  appointment: {
    findMany: jest.Mock;
    findFirst: jest.Mock;
    create: jest.Mock;
    update: jest.Mock;
    delete: jest.Mock;
  };
  session: {
    findUnique: jest.Mock;
    findFirst: jest.Mock;
    findMany: jest.Mock;
    create: jest.Mock;
  };
  payment: {
    findMany: jest.Mock;
    findFirst: jest.Mock;
    create: jest.Mock;
    update: jest.Mock;
    aggregate: jest.Mock;
  };
  $transaction: jest.Mock;
  $connect: jest.Mock;
  $disconnect: jest.Mock;
};

function createMockChain() {
  const mock = jest.fn();
  mock.mockResolvedValue(undefined);
  return mock;
}

export function createPrismaMock(overrides: Partial<PrismaMock> = {}): PrismaMock & Record<string, unknown> {
  const base: PrismaMock = {
    user: {
      findUnique: createMockChain(),
      findFirst: createMockChain(),
      update: createMockChain(),
      create: createMockChain(),
    },
    clinicUser: {
      findUnique: createMockChain(),
      findFirst: createMockChain(),
    },
    refreshToken: {
      findUnique: createMockChain(),
      findFirst: createMockChain(),
      create: createMockChain(),
      updateMany: createMockChain(),
      update: createMockChain(),
    },
    patient: {
      findMany: createMockChain(),
      findFirst: createMockChain(),
      create: createMockChain(),
      update: createMockChain(),
      updateMany: createMockChain(),
      deleteMany: createMockChain(),
    },
    appointment: {
      findMany: createMockChain(),
      findFirst: createMockChain(),
      create: createMockChain(),
      update: createMockChain(),
      delete: createMockChain(),
    },
    session: {
      findUnique: createMockChain(),
      findFirst: createMockChain(),
      findMany: createMockChain(),
      create: createMockChain(),
    },
    payment: {
      findMany: createMockChain(),
      findFirst: createMockChain(),
      create: createMockChain(),
      update: createMockChain(),
      aggregate: createMockChain(),
    },
    $transaction: jest.fn((fn: (tx: unknown) => Promise<unknown>) => fn(base)),
    $connect: jest.fn().mockResolvedValue(undefined),
    $disconnect: jest.fn().mockResolvedValue(undefined),
  };

  return { ...base, ...overrides } as PrismaMock & Record<string, unknown>;
}
