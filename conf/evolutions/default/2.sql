# TickerModel schema

# --- !Ups

ALTER TABLE ticker ADD COLUMN last DECIMAL(20, 8) DEFAULT 0;

# --- !Downs

ALTER TABLE ticker DROP COLUMN last;
