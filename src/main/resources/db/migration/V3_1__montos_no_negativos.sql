ALTER TABLE boleta ADD CONSTRAINT chk_boleta_total CHECK (total >= 0 AND descuento <= subtotal);
