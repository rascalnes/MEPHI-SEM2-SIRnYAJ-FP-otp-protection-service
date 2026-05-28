package ru.nes.otp.service.notification;

/**
 * Интерфейс для сервисов уведомлений.
 */
public interface NotificationService {

    /**
     * Отправка кода получателю.
     * @param destination адрес получателя (email, телефон, chatId и т.д.)
     * @param code OTP-код для отправки
     * @return true если отправка успешна, false в противном случае
     */
    boolean sendCode(String destination, String code);

    /**
     * Получение типа канала.
     */
    String getChannelType();
}