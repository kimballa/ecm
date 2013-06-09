# TickerModel schema

# --- !Ups

CREATE SEQUENCE ticker_seq;
CREATE TABLE ticker (
  id BIGINT NOT NULL DEFAULT nextval('ticker_seq'),
  timestamp TIMESTAMP,
  tradeSymbol CHAR(8),
  quoteSymbol CHAR(8),
  bid DECIMAL(20, 8),
  ask DECIMAL(20, 8),
  high DECIMAL(20, 8),
  low DECIMAL(20, 8),
  volume DECIMAL(20, 8)
);

# --- !Downs

DROP TABLE ticker;
DROP SEQUENCE ticker_seq;
