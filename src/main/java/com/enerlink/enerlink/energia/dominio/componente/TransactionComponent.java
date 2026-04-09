package com.enerlink.enerlink.energia.dominio.componente;

import java.time.LocalDateTime;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

public interface TransactionComponent {

    Long getId();

    EnergyOffer getOffer();

    User getBuyer();

    User getSeller();

    double getKwh();

    double getPrice();

    double getTotalAmount();

    LocalDateTime getTimestamp();
}
