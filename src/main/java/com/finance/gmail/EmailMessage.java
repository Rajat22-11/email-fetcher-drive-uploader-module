package com.finance.gmail;

import lombok.Value;

@Value
public class EmailMessage {
    String id;
    byte[] rawEml;
}
