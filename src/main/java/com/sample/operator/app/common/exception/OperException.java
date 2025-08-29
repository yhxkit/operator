package com.sample.operator.app.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class OperException extends RuntimeException{

    String exMsg;

    public static String getStackTrace(Throwable e)
    {
        String trace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining(
                        System.lineSeparator(), // delimeter
                        e.toString().concat(System.lineSeparator()), // prefix
                         "")); //suffix

        return trace;
    }
}
