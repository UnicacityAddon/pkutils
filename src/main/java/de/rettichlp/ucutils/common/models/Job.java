package de.rettichlp.ucutils.common.models;

import lombok.Getter;

import java.time.Duration;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.configuration;
import static de.rettichlp.ucutils.UCUtils.storage;
import static java.time.Duration.ofMinutes;
import static java.util.regex.Pattern.compile;

@Getter
public enum Job {

    WASTE_COLLECTOR("Müllmann", compile("^\\[Müllmann] Entleere bis zu \\d Mülltonnen an den Haustüren der Häuser und entlade hier alles\\.$")),
    LUMBERJACK("Holzfäller", compile("^\\[Holzfäller] Fälle \\d+ Bäume und bringe sie zu den Sägen zur Weiterverarbeitung!$")),
    MINER("Bergarbeiter", compile("^\\[Steinbruch] Mit dem Zünder kannst du an Erzadern eine Sprengung vornehmen\\. Glück auf!$")),
    URANIUM_TRANSPORT("URAN-Transport", compile("^\\[URAN] Suche Uran-Erz \\(Emerald-Erz\\) und bau es ab\\. Danach musst du es zum Atomkraftwerk bringen\\. \\(/dropuran\\)$")),
    BEVERAGE_SUPPLIER("Getränkelieferant", compile("^\\[Lieferant] Bringe bitte die Bierflaschen zur Bar! \\(/dropdrink\\)$")),
    SUPPLIER("Lieferant", compile("^\\[Transport] Ziel gesetzt: .+$")),
    FARMER("Farmer", compile("^\\[Farmer] Ernte das ganze Weizen und bring es dann zur Mühle\\.$")),
    CASH_TRANSPORT("Geldtransport", compile("^\\[Geldtransport] Bringe das Geld an einen Bankautomaten und benutze /dropmoney$")),
    POWDER_MINE("Pulvermine", compile("^\\[Mine] Baue bitte \\d+ Schwarzpulver-Erze ab! \\(Kohle-Erze\\)$")),
    PAPERBOY("Zeitungsjunge", compile("^\\[Zeitung] Bring bitte das alles zu Häuser deiner Wahl\\.$")),
    TOBACCO_PLANTATION("Tabakplantage", compile("^\\[Tabakplantage] Ernte Tabak und lege es zum trocknen auf die Steintische\\.$")),
    PIZZA_DELIVERY("Pizzalieferant", compile("^\\[Pizzalieferant] Hole nun die Pizzen in der Küche und bringe sie zu den wartenden Kunden\\. Du kannst die Pizzen mit /getpizza abholen\\.$")),
    DEEP_SEA_FISHER("Hochseefischer", compile("^\\[Fischer] Fahre nun zu den Fischschwärmen und wirf dein Fischenetz mit /catchfish aus\\.$")),
    WINEMAKER("Winzer", compile("^\\[Winzer] Gehe nun zum Rebstock und sammel die Trauben\\.$")),
    TABLE_CLEANER("Tellerwäscher", compile("^\\[TableCleaner] Du hast die Arbeit im Dönerladen als Putzkraft begonnen! Reinige \\d+ Tische\\.$")),
    GARDENER("Gärtner", compile("^\\[Gärtner] Pflücke \\d+ Blumen an den markierten Stellen\\.$"));

    private static final Duration DEFAULT_COOLDOWN = ofMinutes(20);
    private static final Duration PREMIUM_COOLDOWN = ofMinutes(15);

    private final String displayName;
    private final Pattern jobStartPattern;

    Job(String displayName, Pattern jobStartPattern) {
        this.displayName = displayName;
        this.jobStartPattern = jobStartPattern;
    }

    public Duration getCooldown() {
        return storage.isPremium() ? PREMIUM_COOLDOWN : DEFAULT_COOLDOWN;
    }

    public void startCountdown() {
        if (!configuration.getOptions().notification().jobCooldown()) {
            return;
        }

        new Countdown(this.displayName, getCooldown(), () -> {});
    }
}
