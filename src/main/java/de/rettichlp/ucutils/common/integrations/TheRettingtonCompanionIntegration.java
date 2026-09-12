package de.rettichlp.ucutils.common.integrations;

import de.rettichlp.therettingtoncompanion.TheRettingtonCompanionApi;
import de.rettichlp.therettingtoncompanion.gui.options.list.HiddenMessageEntry;
import de.rettichlp.therettingtoncompanion.gui.widgets.base.AbstractTRCWidget;
import de.rettichlp.therettingtoncompanion.models.Notification;
import de.rettichlp.ucutils.common.gui.widgets.CarLockedWidget;
import de.rettichlp.ucutils.common.gui.widgets.CorpseCountWidget;
import de.rettichlp.ucutils.common.gui.widgets.MedicStatusWidget;
import de.rettichlp.ucutils.common.gui.widgets.MoneyWidget;
import de.rettichlp.ucutils.common.gui.widgets.PayDayWidget;
import de.rettichlp.ucutils.common.gui.widgets.ServiceCountWidget;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.MOD_ID;
import static de.rettichlp.ucutils.UCUtils.notificationService;
import static java.util.regex.Pattern.compile;

public class TheRettingtonCompanionIntegration implements TheRettingtonCompanionApi {

    @Override
    public Set<Notification> getNotifications() {
        return notificationService.getNotifications();
    }

    @Override
    public List<AbstractTRCWidget<?>> getWidgets() {
        return List.of(
                new CarLockedWidget(),
                new CorpseCountWidget(),
                new MedicStatusWidget(),
                new MoneyWidget(),
                new PayDayWidget(),
                new ServiceCountWidget()
        );
    }

    @Override
    public List<HiddenMessageEntry.HiddenMessage> getHiddenMessages() {
        return List.of(
                // join
                toHiddenMessage(compile("^» Dein Account wird nicht mit einem Passwort geschützt!$")),
                toHiddenMessage(compile("^» Schütze deinen Account mit /passwort new \\[Passwort]\\.$")),
                // rotators
                toHiddenMessage(compile("^\\[F\\.A\\.Q] Du hast Fragen zum Server\\? Dann schau doch mal in unser FAQ rein\\.\\n\\[F\\.A\\.Q] Dort sind viele Antworten auf all deine Fragen\\.$")),
                toHiddenMessage(compile("^\\[Shop] Hast du schon die tollen Sachen in unserem Shop entdeckt\\?\\n\\[Shop] Wir bedanken uns bei allen Unterstützern!$")),
                toHiddenMessage(compile("^\\[Voten] Du kannst uns täglich mit deinen Votes unterstützen!\\n\\[Voten] Sammle genug Votepunkte um dir tolle Geschenke im Voteshop zu holen!$")),
                toHiddenMessage(compile("^\\[Forum] Hast du schon einen Account in unserem Forum\\?\\n\\[Forum] Werde noch heute ein Teil dieser großartigen Community!$")),
                toHiddenMessage(compile("^\\[Regeln] Unwissenheit schützt vor Strafe nicht!\\n\\[Regeln] Informiere dich in unserem Regelwerk\\.$")),
                toHiddenMessage(compile("^\\[TikTok] Kennst du schon unseren TikTok Account\\?\\n\\[TikTok] Folge uns jetzt auf TikTok!$")),
                toHiddenMessage(compile("^\\[Discord] Kennst du schon unseren Community-Discord\\?\\n\\[Discord] Updates, Ankündigungen und vieles mehr findest du dort!$")),
                toHiddenMessage(compile("^\\[LabyMod] Wir sind offizieller Partner von LabyMod!\\n\\[LabyMod] Schau dir doch mal die LabyMod Client-Mod an\\.$")),
                toHiddenMessage(compile("^\\[Premium] Noch keinen Premium-Account\\?\\n\\[Premium] Besuch doch mal unseren Shop!$")),
                toHiddenMessage(compile("^\\[Passwort] Ist dein Account schon mit einen Passwort geschützt\\?\\n\\[Passwort] Schütze deinen Account noch heute mit einen Passwort!$")),
                toHiddenMessage(compile("^\\[Instagram] Folgst du schon dem offiziellen UnicaCity Instagram-Account\\?\\n\\[Instagram] Du kannst uns unter dem Namen UnicaCity oder @unicacityeu finden!$")),
                toHiddenMessage(compile("^\\[Teamspeak] Besuche uns doch auf unserem Teamspeak Server\\.\\n\\[Teamspeak] Die IP ist: unicacity\\.eu$")),
                toHiddenMessage(compile("^\\[BattlePass] Hol dir tolle Belohnungen mit unserem BattlePass!\\n\\[BattlePass] Öffne ihn jederzeit mit /battlepass\\.$")),
                // vote
                toHiddenMessage(compile("^ » Mit deinen Votes kannst du uns täglich unterstützen!$")),
                toHiddenMessage(compile("^ » Du siehst bei /vote alle Seiten bei welchen du voten kannst\\.$")),
                toHiddenMessage(compile("^ » Du erhältst diese Nachricht nach dem Voten nicht mehr\\.$")),
                // afk
                toHiddenMessage(compile("^ {2}Info: Deine PayDay-Zeit läuft nun weiter\\.$")),
                // rubbish
                toHiddenMessage(compile("^Du hast etwas aus dem Mülleimer genommen\\.$")),
                toHiddenMessage(compile("^Du durchwühlst den Mülleimer\\.$")),
                // cooking
                toHiddenMessage(compile("^\\[Küche] \uD83C\uDF73 Klicke das schwebende Item bevor es verschwindet!$")),
                toHiddenMessage(compile("^\\[Küche] ✔ Erwischt! Qualität steigt\\.$")),
                // lumberjack
                toHiddenMessage(compile("^\\[Holzfäller] \\d Perk\\(s\\) geladen\\.$")),
                // faction: medics
                toHiddenMessage(compile("^\\[ʟᴀʙᴏʀ] \\d+ Kräuter-Rezept\\(e\\) ausgegeben \\(\\d+g Gras verbraucht · \\d+g verbleibend\\)\\.$")),
                toHiddenMessage(compile("^\\[Tierarzt] Standort: -?\\d+ -?\\d+ -?\\d+ – behandle das Tier per Sneak-Rechtsklick\\.$"))
        );
    }

    @Contract(value = "_ -> new", pure = true)
    private HiddenMessageEntry.@NonNull HiddenMessage toHiddenMessage(@NonNull Pattern pattern) {
        return new HiddenMessageEntry.HiddenMessage(pattern.pattern(), MOD_ID);
    }
}
