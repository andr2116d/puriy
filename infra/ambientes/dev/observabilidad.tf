module "observabilidad" {
  source = "../../modulos/observabilidad"

  nombre           = local.nombre
  colas_de_errores = module.eventos.colas_de_errores
}
