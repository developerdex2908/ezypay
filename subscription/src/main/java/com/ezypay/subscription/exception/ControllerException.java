package com.ezypay.subscription.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ControllerException {
    String errorMessage = "";
    String errorCode = "";
}
