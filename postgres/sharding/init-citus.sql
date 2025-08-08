CREATE EXTENSION IF NOT EXISTS citus;
SELECT citus_set_coordinator_host('coordinator');
SELECT master_add_node('worker1', 5432);
SELECT master_add_node('worker2', 5432);