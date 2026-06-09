-- Limpa todas as comandas (pedidos) acumuladas pelos testes de UI e libera as mesas.
-- Mantém usuários, cardápio e adicionais (apenas os dados das comandas são apagados).
-- Ordem respeita as foreign keys: addons -> itens -> comandas.

BEGIN;

DELETE FROM order_item_addons;
DELETE FROM order_items;
DELETE FROM orders;

-- Libera todas as mesas que ficaram presas em comandas abertas pelos testes.
UPDATE restaurant_tables SET status = 'AVAILABLE';

COMMIT;
