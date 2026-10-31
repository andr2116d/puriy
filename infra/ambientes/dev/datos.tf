module "datos" {
  source = "../../modulos/datos"

  nombre   = local.nombre
  vpc_id   = module.red.vpc_id
  subredes = module.red.subredes_datos
}
