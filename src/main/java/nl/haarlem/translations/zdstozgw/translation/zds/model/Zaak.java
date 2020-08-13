package nl.haarlem.translations.zdstozgw.translation.zds.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.STUF;
import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Zaak {
    @XmlAttribute(namespace = STUF)
    public String entiteittype = "ZAK";

    @XmlElement(namespace = ZKN)
    public String identificatie;

    @XmlElement(namespace = ZKN, nillable = true)
    public String einddatum;

    @XmlElement(namespace = ZKN, nillable = true)
    public String einddatumGepland;

    @XmlElement(namespace = ZKN, nillable = true)
    public String omschrijving;

    @XmlElement(namespace = ZKN, nillable = true)
    public List<Kenmerk> kenmerk;

    @XmlElement(namespace = ZKN, nillable = true)
    public Resultaat resultaat;

    @XmlElement(namespace = ZKN, nillable = true)
    public String startdatum;

    @XmlElement(namespace = ZKN, nillable = true)
    public String toelichting;

    @XmlElement(namespace = ZKN, nillable = true)
    public String uiterlijkeEinddatum;

    @XmlElement(namespace = ZKN, nillable = true)
    public String zaakniveau;

    @XmlElement(namespace = ZKN, nillable = true)
    public String deelzakenIdicatie;

    @XmlElement(namespace = ZKN, nillable = true)
    public String registratiedatum;

    @XmlElement(namespace = ZKN, nillable = true)
    public String publicatiedatum;

    @XmlElement(namespace = ZKN, nillable = true)
    public String archiefnominatie;

    @XmlElement(namespace = ZKN, nillable = true)
    public String datumVernietigingDossier;

    @XmlElement(namespace = ZKN, nillable = true)
    public String betalingsIndicatie;

    @XmlElement(namespace = ZKN, nillable = true)
    public String laatsteBetaaldatum;

    @XmlElement(namespace = ZKN, nillable = true)
    public Opschorting opschorting;

    @XmlElement(namespace = ZKN, nillable = true)
    public Verlenging verlenging;

    @XmlElement(namespace = ZKN, nillable = true)
    public AnderZaakObject anderZaakObject;

    @XmlElement(namespace = ZKN, nillable = true)
    public Rol heeftBetrekkingOp;

    @XmlElement(namespace = ZKN, nillable = true)
    public Rol heeftAlsBelanghebbende;

    @XmlElement(namespace = ZKN, nillable = true)
    public Rol heeftAlsGemachtigde;

    @XmlElement(namespace = ZKN, nillable = true)
    public Rol heeftAlsInitiator;

    @XmlElement(namespace = ZKN, nillable = true)
    public Rol heeftAlsUitvoerende;

    @XmlElement(namespace = ZKN, nillable = true)
    public Rol heeftAlsVerantwoordelijke;

    @XmlElement(namespace = ZKN, nillable = true)
    public Rol heeftAlsOverigBetrokkene;

    @XmlElement(namespace = ZKN)
    public Rol isVan;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Resultaat {
        @XmlElement(namespace = ZKN)
        private String omschrijving;

        @XmlElement(namespace = ZKN)
        private String toelichting;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AnderZaakObject {
        @XmlElement(namespace = ZKN)
        public String omschrijving;

        @XmlElement(namespace = ZKN)
        public String aanduiding;

        @XmlElement(namespace = ZKN)
        public String lokatie;

        @XmlElement(namespace = ZKN)
        public String registratie;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Verlenging {
        @XmlElement(namespace = ZKN)
        public String duur;

        @XmlElement(namespace = ZKN)
        public String reden;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Opschorting {
        @XmlElement(namespace = ZKN)
        public String indicatie;

        @XmlElement(namespace = ZKN)
        public String reden;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Kenmerk {
        @XmlElement(namespace = ZKN)
        public String kenmerk;

        @XmlElement(namespace = ZKN)
        public String bron;
    }
}

