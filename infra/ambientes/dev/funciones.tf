module "funciones" {
  source = "../../modulos/funciones"

  nombre             = local.nombre
  ruta_paquetes      = "${path.root}/../../../backend/funciones"
  colas              = module.eventos.colas
  bucket_boletos_arn = module.eventos.bucket_boletos_arn
}
