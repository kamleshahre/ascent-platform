default_user = {{ with secret "secret/application" }}{{ index .Data "spring.rabbitmq.username" }}{{ end }}
default_pass = {{ with secret "secret/application" }}{{ index .Data "spring.rabbitmq.password" }}{{ end }}
loopback_users = none
cluster_formation.peer_discovery_backend  = rabbit_peer_discovery_consul
cluster_formation.consul.host = consul
cluster_formation.node_cleanup.only_log_warning = false
cluster_formation.consul.svc_addr_auto = true
cluster_partition_handling = autoheal
vm_memory_high_watermark.relative = 0.8