package com.movies.entity;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieXML {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @ApiModelProperty("Represents the ID which is unique to a movie")
    private Long movie_id;

    @ApiModelProperty("Represents name of a  movie")
    @NotBlank
    private String name;

    @ApiModelProperty("Represents the year the movie got released")
    @NotNull
    private Integer year;

    @ApiModelProperty("Represents the cast of the movie")
    @NotBlank
    private String cast;

    @NotNull
    @ApiModelProperty("Represents the release date of the movie")
    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDate release_date;
}
