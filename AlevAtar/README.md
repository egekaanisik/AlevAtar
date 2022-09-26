# AlevAtar
Boş vaktimde yazdığım, arka arkaya kalp veya alev emojisi gönderen bir Discord botu.

## Gereksinimler
* Java 1.8+
* Gradle 7+
* JDA 5+

## Yükleme
Botu kullanmak için [Discord Geliştirici Portalı](https://discord.com/developers/applications) üzerinden yeni bir bot oluşturmanız gerekmekte. Bot için OAuth2 linki oluştururken `bot` ve `applications.commands` scopelarının aktive edilmesi gerekiyor.

Botu ilk defa çalıştırdıktan sonra oluşacak `config.json` dosyasının `token` alanına geliştirici portalının Bot sekmesinden alınan token yazılmalı.

Doldurulması gereken diğer bir alan ise `owner_id`. Bot sahibine özel komutları kullanmak için bu alana bot sahibinin ID'si girilmeli.

> Not: Kullanıcı ID'si Discord'da geliştirici modunu açtıktan sonra alınabilir. Açtıktan sonra kullanıcı profiline sağ tıklayın ve and "ID'yi Kopyala" tuşuna basın.

## JAR Oluşturma
JAR dosyası oluşturmak için proje klasöründe aşağıda yer alan komutu çalıştırın. Oluşturulan dosya `build/libs` klasörüne kaydedilecek.
```
gradlew shadowjar
```