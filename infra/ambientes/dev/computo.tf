module "computo" {
  source = "../../modulos/computo"

  nombre              = local.nombre
  vpc_id              = module.red.vpc_id
  subredes_publicas   = module.red.subredes_publicas
  subredes_aplicacion = module.red.subredes_aplicacion

  base = {
    direccion          = module.datos.direccion
    nombre             = module.datos.base
    secreto_arn        = module.datos.secreto_arn
    grupo_seguridad_id = module.datos.grupo_seguridad_id
  }
}
