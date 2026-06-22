-- ============================================
-- AutoMind — Dados Iniciais (seed)
-- ============================================

-- Serviços padrão da oficina
INSERT INTO servicos (nome, descricao, tipo, preco_base, tempo_estimado_horas, ativo) VALUES
  ('Troca de Óleo', 'Troca de óleo do motor com filtro', 'TROCA_OLEO', 120.00, 0.5, true),
  ('Troca de Correia Dentada', 'Substituição da correia dentada e tensionador', 'TROCA_CORREIA_DENTADA', 450.00, 3.0, true),
  ('Troca de Pastilhas de Freio', 'Substituição das pastilhas de freio dianteiras', 'TROCA_PASTILHA_FREIO', 200.00, 1.5, true),
  ('Troca de Pneus', 'Montagem e balanceamento de 4 pneus', 'TROCA_PNEU', 80.00, 1.0, true),
  ('Revisão Geral', 'Revisão completa do veículo: fluidos, filtros, freios, suspensão', 'REVISAO_GERAL', 350.00, 4.0, true),
  ('Alinhamento', 'Alinhamento da direção', 'ALINHAMENTO', 70.00, 0.5, true),
  ('Balanceamento', 'Balanceamento de 4 rodas', 'BALANCEAMENTO', 60.00, 0.5, true)
ON CONFLICT DO NOTHING;

-- Peças básicas
INSERT INTO pecas (codigo, nome, descricao, preco_compra, preco_venda, quantidade_estoque, estoque_minimo, fabricante, ativo) VALUES
  ('FIL-001', 'Filtro de Óleo Universal', 'Filtro de óleo compatível com motores 1.0 a 2.0', 18.00, 35.00, 50, 10, 'Fram', true),
  ('FIL-002', 'Filtro de Ar', 'Filtro de ar para motores a gasolina', 22.00, 42.00, 30, 8, 'Mann', true),
  ('PAD-001', 'Pastilha de Freio Dianteira', 'Jogo de pastilhas dianteiras', 65.00, 130.00, 20, 5, 'Bosch', true),
  ('PAD-002', 'Pastilha de Freio Traseira', 'Jogo de pastilhas traseiras', 55.00, 110.00, 15, 5, 'Bosch', true),
  ('OLE-001', 'Óleo Motor 5W30 Sintético 4L', 'Óleo sintético para motores flex', 85.00, 160.00, 40, 10, 'Mobil', true),
  ('OLE-002', 'Óleo Motor 5W40 Sintético 4L', 'Óleo sintético para motores turbo', 95.00, 180.00, 25, 8, 'Castrol', true),
  ('COR-001', 'Correia Dentada', 'Correia dentada universal', 120.00, 240.00, 10, 3, 'Gates', true),
  ('VEL-001', 'Vela de Ignição', 'Vela de ignição iridium', 45.00, 90.00, 60, 15, 'NGK', true)
ON CONFLICT DO NOTHING;
