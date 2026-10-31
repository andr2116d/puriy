variable "nombre" {
  description = "Nombre base de los recursos"
  type        = string
}

variable "alb_dominio" {
  description = "Dirección del ALB"
  type        = string
}

variable "limite_por_ip" {
  description = "Solicitudes por IP cada 5 minutos"
  type        = number
  default     = 2000
}
