# TickerModel schema

# --- !Ups

CREATE INDEX ticker_timestamp_idx ON ticker(timestamp);

# --- !Downs

DROP INDEX ticker_timestamp_idx;
