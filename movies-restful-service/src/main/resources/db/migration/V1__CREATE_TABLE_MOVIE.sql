CREATE TABLE MOVIE(
    movie_id INT PRIMARY KEY NOT NULL,
    name VARCHAR(1000),
    year INTEGER NOT NULL,
    cast  VARCHAR(1000)    NOT NULL,
    release_date   DATE NOT NULL
);