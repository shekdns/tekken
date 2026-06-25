package com.project.tekken.character;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TekkenCharacterCatalogService {

    private static final List<TekkenCharacterOption> CHARACTERS = List.of(
            character("alisa", "Alisa", "알리사", "アリサ", "Alisa Bosconovitch"),
            character("anna", "Anna", "안나", "アンナ", "Anna Williams"),
            character("armor-king", "Armor King", "아머 킹", "アーマーキング", "Armor King II"),
            character("asuka", "Asuka", "아스카", "飛鳥", "Asuka Kazama"),
            character("azucena", "Azucena", "아주세나", "アズセナ", "Azucena Milagros Ortiz Castillo"),
            character("bryan", "Bryan", "브라이언", "ブライアン", "Bryan Fury"),
            character("claudio", "Claudio", "클라우디오", "クラウディオ", "Claudio Serafino"),
            character("clive", "Clive", "클라이브", "クライヴ", "Clive Rosfield"),
            character("devil-jin", "Devil Jin", "데빌 진", "デビル仁"),
            character("dragunov", "Dragunov", "드라구노프", "ドラグノフ", "Sergei Dragunov"),
            character("eddy", "Eddy", "에디", "エディ", "Eddy Gordo"),
            character("fahkumram", "Fahkumram", "파쿰람", "ファーカムラム"),
            character("feng", "Feng", "펭", "フェン", "Feng Wei"),
            character("heihachi", "Heihachi", "헤이하치", "平八", "Heihachi Mishima"),
            character("hwoarang", "Hwoarang", "화랑", "ファラン"),
            character("jack-8", "JACK-8", "잭-8", "ジャック-8", "Jack-8", "Jack"),
            character("jin", "Jin", "진", "仁", "Jin Kazama"),
            character("jun", "Jun", "준", "準", "Jun Kazama"),
            character("kazuya", "Kazuya", "카즈야", "一八", "Kazuya Mishima"),
            character("king", "King", "킹", "キング", "King II"),
            character("kuma", "Kuma", "쿠마", "クマ", "Kuma II"),
            character("kunimitsu", "Kunimitsu", "쿠니미츠", "州光", "Kunimitsu II"),
            character("lars", "Lars", "라스", "ラース", "Lars Alexandersson"),
            character("law", "Law", "로우", "ロウ", "Marshall Law"),
            character("lee", "Lee", "리", "リー", "Lee Chaolan"),
            character("leo", "Leo", "레오", "レオ", "Leo Kliesen"),
            character("leroy", "Leroy", "리로이", "リロイ", "Leroy Smith"),
            character("lidia", "Lidia", "리디아", "リディア", "Lidia Sobieska"),
            character("lili", "Lili", "리리", "リリ", "Emilie De Rochefort", "Lili De Rochefort"),
            character("miary-zo", "Miary Zo", "미아리 조", "ミアリ・ゾ"),
            character("nina", "Nina", "니나", "ニーナ", "Nina Williams"),
            character("panda", "Panda", "판다", "パンダ"),
            character("paul", "Paul", "폴", "ポール", "Paul Phoenix"),
            character("raven", "Raven", "레이븐", "レイヴン"),
            character("reina", "Reina", "레이나", "麗奈"),
            character("shaheen", "Shaheen", "샤힌", "シャヒーン"),
            character("steve", "Steve", "스티브", "スティーブ", "Steve Fox"),
            character("victor", "Victor", "빅터", "ヴィクター", "Victor Chevalier"),
            character("xiaoyu", "Xiaoyu", "샤오유", "シャオユウ", "Ling Xiaoyu"),
            character("yoshimitsu", "Yoshimitsu", "요시미츠", "吉光"),
            character("zafina", "Zafina", "자피나", "ザフィーナ")
    ).stream()
            .sorted(Comparator.comparing(TekkenCharacterOption::displayName))
            .toList();

    public TekkenCharacterOptionsResponse getCharacterOptions() {
        return new TekkenCharacterOptionsResponse(CHARACTERS);
    }

    private static TekkenCharacterOption character(String id, String name, String koreanName, String japaneseName, String... aliases) {
        return new TekkenCharacterOption(
                id,
                name,
                name,
                Map.of(
                        "ko", koreanName,
                        "en", name,
                        "ja", japaneseName),
                id,
                null,
                List.of(aliases));
    }
}
