package com.example.message.dto;

import com.example.validator.ValidBody;

/**
 * POST /messagesのリクエスト本体。
 * 
 * @param body メッセージ本文
 */
public record MessageRequest(@ValidBody String body) {
}
