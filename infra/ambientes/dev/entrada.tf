module "entrada_global" {
  source = "../../modulos/entrada-global"

  nombre      = local.nombre
  alb_dominio = module.computo.alb_dominio

  providers = {
    aws        = aws
    aws.global = aws.global
  }
}
