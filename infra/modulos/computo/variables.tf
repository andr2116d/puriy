variable "nombre" {
  description = "Nombre base de los recursos"
  type        = string
}

variable "vpc_id" {
  description = "VPC"
  type        = string
}

variable "subredes_publicas" {
  description = "Subredes del ALB"
  type        = list(string)
}

variable "subredes_aplicacion" {
  description = "Subredes de las EC2"
  type        = list(string)
}

variable "base" {
  description = "Conexión a PostgreSQL"
  type = object({
    direccion          = string
    nombre             = string
    secreto_arn        = string
    grupo_seguridad_id = string
  })
}

variable "tipo_instancia" {
  description = "Tipo de EC2"
  type        = string
  default     = "t4g.small"
}

variable "minimo_instancias" {
  description = "Mínimo de EC2"
  type        = number
  default     = 2
}

variable "maximo_instancias" {
  description = "Máximo de EC2"
  type        = number
  default     = 6
}

variable "cpu_objetivo" {
  description = "Porcentaje de CPU objetivo"
  type        = number
  default     = 60
}
