CREATE TABLE IF NOT EXISTS country (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS city (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
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

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('New York', 1, 40.69983071061861, -73.86707116211049) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Los Angeles', 1, 40.80070219029437, -74.00731803377057) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Chicago', 1, 41.88299235457815, -87.67408153230005) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Toronto', 2, 43.68203518783054, -79.61460650018297) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Vancouver', 2, 49.24339274864671, -123.13162876315121) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Sydney', 3, -33.847962849461965, 151.06700330121473) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Melbourne', 3, -37.78623340876409, 145.05853650646864) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Paris', 4, 48.847333766634385, 2.3605962418146214) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Marseille', 4, 43.277504319174575, 5.439924076435636) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Berlin', 5, 52.472884142842446, 13.43772797702345) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Munich', 5, 48.16498739285805, 11.499607044741092) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('London', 6, 51.511123975634625, -0.07447616468600105) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Manchester', 6, 53.47020190228331, -2.235994134714431) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Sao Paulo', 7, -23.633483053299532, -46.62636157114068) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Rio de Janeiro', 7, -22.9285039812354, -43.44063650365003) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Mumbai', 8, 19.135089835444504, 72.8727745854004) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Delhi', 8, 28.67010806622853, 77.13576731241571) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Tokyo', 9, 35.72684568738373, 139.4620939633352) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Osaka', 9, 34.68860851668591, 135.52569111978778) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Beijing', 10, 40.01578254297966, 116.37861980202165) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Shanghai', 10, 31.115417220586753, 121.58672271898229) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Moscow', 11, 55.56865072153131, 37.48502555365702) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Saint Petersburg', 11, 59.98097719280576, 30.269747999088512) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Rome', 12, 41.90349983179419, 12.454522549812399) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Milan', 12, 45.476457626194865, 9.188203589075243) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Madrid', 13, 40.4173456637589, -3.684863267234313) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Barcelona', 13, 41.433606969888096, 2.18705264867276) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Mexico City', 14, 19.431684127185576, -99.10595143491035) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Guadalajara', 14, 20.66712509517505, -103.29677419868456) ON CONFLICT (name, country_id) DO NOTHING;

INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Belgrade', 15, 44.83131723791987, 20.446972900334423) ON CONFLICT (name, country_id) DO NOTHING;
INSERT INTO city (name, country_id, latitude, longitude) VALUES ('Novi Sad', 15, 45.25840280007606, 19.8516726409658) ON CONFLICT (name, country_id) DO NOTHING;
