package de.rettichlp.pkutils.common.models;

import lombok.Getter;

import java.time.Duration;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.time.Duration.ofMinutes;
import static java.util.regex.Pattern.compile;

@Getter
public enum Job {

    WASTE_COLLECTOR("Müllmann", compile("^\\[Müllmann] Entleere bis zu \\d Mülltonnen an den Türen der Häuser und entlade hier alles\\.$")),
    LUMBERJACK("Holzfäller", ofMinutes(20), compile("^\\[Holzfäller] Fälle \\d+ Bäume und bringe sie zu den Sägen zur Weiterverarbeitung!$")),
    MINER("Bergarbeiter", ofMinutes(20), compile("^\\[Steinbruch] Mit dem Zünder kannst du an Erzadern eine Sprengung vornehmen\\. Glück auf!$")),
    URANIUM_TRANSPORT("URAN-Transport", ofMinutes(20), compile("^\\[URAN] Suche Uran-Erz \\(Emerald-Erz\\) und bau es ab\\. Danach musst du es zum Atomkraftwerk bringen\\. \\(/dropuran\\)$")),
    BEVERAGE_SUPPLIER("Getränkelieferant", ofMinutes(10), compile("^\\[Lieferant] Bringe bitte die Bierflaschen zur Bar! \\(/dropdrink\\)$")),
    SUPPLIER("Lieferant", compile("^\\[Transport] Liefere (die Kisten|die Waffenkisten|das Weizen) (zu einem Geschäft deiner Wahl|zu einem Waffenladen deiner Wahl|zur Bäckerei), benutze dazu /droptransport$")),
    FARMER("Farmer", compile("^\\[Farmer] Ernte das ganze Weizen und bring es dann zur Mühle\\.$")),
    CASH_TRANSPORT("Geldtransport", compile("^\\[Geldtransport] Bringe das Geld an einen Bankautomaten und benutze /dropmoney\\.$")),
    POWDER_MINE("Pulvermine", compile("^\\[Mine] Baue bitte \\d+ Schwarzpulver-Erze ab! \\(Kohle-Erze\\)$")),
    PAPERBOY("Zeitungsjunge", ofMinutes(12), compile("^\\[Zeitung] Bring bitte das alles zu Häuser deiner Wahl\\.$")),
    TOBACCO_PLANTATION("Tabakplantage", compile("^\\[Tabakplantage] Ernte Tabak und lege es zum Trocknen auf die Steintische\\.$")),
    PIZZA_DELIVERY("Pizzalieferant", compile("^\\[Pizzalieferant] Hole nun die Pizzen in der Küche und bringe sie zu den wartenden Kunden\\.$")),
    DEEP_SEA_FISHER("Hochseefischer", ofMinutes(20), compile("^\\[Fischer] Fahre nun zu den Fischschwärmen und wirf dein Fischernetz mit /catchfish aus\\.$")),
    WINEMAKER("Winzer", ofMinutes(20), compile("^\\[Winzer] Gehe nun zum Rebstock und sammel die Trauben\\.$"));

    private final String displayName;
    private final Duration cooldown;
    private final Pattern jobStartPattern;

    Job(String displayName, Pattern jobStartPattern) {
        this.displayName = displayName;
        this.jobStartPattern = jobStartPattern;
        this.cooldown = ofMinutes(15);
    }

    Job(String displayName, Duration cooldown, Pattern jobStartPattern) {
        this.displayName = displayName;
        this.cooldown = cooldown;
        this.jobStartPattern = jobStartPattern;
    }

    public void startCountdown() {
        storage.getCountdowns().add(new Countdown(this.displayName, this.cooldown, () -> notificationService.sendInfoNotification("Cooldown für '" + this.displayName + "' abgelaufen")));
    }
}
