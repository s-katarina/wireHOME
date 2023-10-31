CREATE TABLE IF NOT EXISTS country (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS city (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country_id INT NOT NULL,
    UNIQUE (name, country_id),
    FOREIGN KEY (country_id) REFERENCES country(id)
);


INSERT INTO country (name) VALUES ('USA') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Canada') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Australia') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('France') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Germany') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('United Kingdom') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Brazil') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('India') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Japan') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('China') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Russia') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Italy') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Spain') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Mexico') ON CONFLICT (name) DO NOTHING;
INSERT INTO country (name) VALUES ('Serbia') ON CONFLICT (name) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('New York', 1) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Los Angeles', 1) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Chicago', 1) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Toronto', 2) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Vancouver', 2) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Sydney', 3) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Melbourne', 3) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Paris', 4) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Marseille', 4) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Berlin', 5) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Munich', 5) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('London', 6) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Manchester', 6) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Sao Paulo', 7) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Rio de Janeiro', 7) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Mumbai', 8) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Delhi', 8) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Tokyo', 9) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Osaka', 9) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Beijing', 10) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Shanghai', 10) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Moscow', 11) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Saint Petersburg', 11) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Rome', 12) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Milan', 12) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Madrid', 13) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Barcelona', 13) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Mexico City', 14) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Guadalajara', 14) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id) VALUES ('Belgrade', 15) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id) VALUES ('Novi Sad', 15) ON CONFLICT (name, country_id) DO NOTHING;
