package com.polykhel.ssa.web.rest.vm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * View Model object for representing a user, with his authorities.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class UserVM {

    @NotNull
    @Size(min = 1, max = 50)
    private String login;

    private Set<String> authorities;
}
