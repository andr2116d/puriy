module "tiempo_real" {
  source = "../../modulos/tiempo-real"

  nombre            = local.nombre
  grupo_usuarios_id = module.identidad.grupo_usuarios_id
}
