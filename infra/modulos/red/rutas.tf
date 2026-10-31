resource "aws_eip" "nat" {
  for_each = var.subredes

  domain = "vpc"

  tags = {
    Name = "${var.nombre}-nat-${each.key}"
  }

  depends_on = [aws_internet_gateway.principal]
}

resource "aws_nat_gateway" "salida" {
  for_each = var.subredes

  allocation_id = aws_eip.nat[each.key].id
  subnet_id     = aws_subnet.publica[each.key].id

  tags = {
    Name = "${var.nombre}-${each.key}"
  }
}

resource "aws_route_table" "publica" {
  vpc_id = aws_vpc.principal.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.principal.id
  }

  tags = {
    Name = "${var.nombre}-publica"
  }
}

resource "aws_route_table_association" "publica" {
  for_each = var.subredes

  subnet_id      = aws_subnet.publica[each.key].id
  route_table_id = aws_route_table.publica.id
}

resource "aws_route_table" "aplicacion" {
  for_each = var.subredes

  vpc_id = aws_vpc.principal.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.salida[each.key].id
  }

  tags = {
    Name = "${var.nombre}-aplicacion-${each.key}"
  }
}

resource "aws_route_table_association" "aplicacion" {
  for_each = var.subredes

  subnet_id      = aws_subnet.aplicacion[each.key].id
  route_table_id = aws_route_table.aplicacion[each.key].id
}

resource "aws_route_table" "datos" {
  vpc_id = aws_vpc.principal.id

  tags = {
    Name = "${var.nombre}-datos"
  }
}

resource "aws_route_table_association" "datos" {
  for_each = var.subredes

  subnet_id      = aws_subnet.datos[each.key].id
  route_table_id = aws_route_table.datos.id
}

data "aws_region" "actual" {}

resource "aws_vpc_endpoint" "s3" {
  vpc_id          = aws_vpc.principal.id
  service_name    = "com.amazonaws.${data.aws_region.actual.region}.s3"
  route_table_ids = [for tabla in aws_route_table.aplicacion : tabla.id]

  tags = {
    Name = "${var.nombre}-s3"
  }
}
