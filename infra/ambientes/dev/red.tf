module "red" {
  source = "../../modulos/red"

  nombre = local.nombre
  cidr   = "10.20.0.0/16"

  subredes = {
    "${var.region}a" = {
      publica    = "10.20.0.0/24"
      aplicacion = "10.20.10.0/24"
      datos      = "10.20.20.0/24"
    }
    "${var.region}b" = {
      publica    = "10.20.1.0/24"
      aplicacion = "10.20.11.0/24"
      datos      = "10.20.21.0/24"
    }
  }
}
