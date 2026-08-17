# Nova F-R (Nova First Response)

**Offline AI first aid, for anyone, anywhere — no internet, no registration, pure humanity.**

Nova F-R is a free, privacy-focused mobile app that gives people step-by-step first-aid guidance during emergencies where cellular networks, internet access, or emergency services are unavailable — natural disasters, war zones, infrastructure blackouts, or remote/wilderness situations.

---

## Why this exists

In a real crisis, the people who need help most are often exactly the people who *can't* get online. Nova F-R runs a fully local AI model directly on your phone — no server, no API calls, no data leaving your device. Once installed, it works completely offline.

- **No internet required after install** — the AI runs entirely on-device
- **No email, no registration, no accounts** — open the app and start asking
- **No data collection** — nothing you type ever leaves your phone
- **Free and open source** — built for the people who need it, not for profit

---

## Meet Atlas

The AI inside Nova F-R is called **Atlas** — a small language model (Qwen2.5-1.5B-Instruct) fine-tuned specifically for first-aid and emergency response scenarios, trained on the [FirstAidQA dataset](https://huggingface.co/datasets/i-am-mushfiq/FirstAidQA) (5,500 question-answer pairs derived from the certified *Vital First Aid Book*, presented at the NeurIPS 2025 Workshop on Muslims in ML).

- **Model size:** ~986MB (Q4_K_M quantization, GGUF format)
- **Hosted at:** [Kami574/Atlas-FirstAid](https://huggingface.co/Kami574/Atlas-FirstAid) on Hugging Face
- **Runs via:** [llama.cpp](https://github.com/ggerganov/llama.cpp), fully on-device

### Device requirements
| | Minimum |
|---|---|
| RAM | 3 GB |
| Storage | ~1.2 GB free (app + model) |
| OS | Android 8.0 (API 26) or higher |
| Internet | Only required once, to download the AI model on first launch |

---

## ⚠️ Important safety disclaimer

**Atlas can make mistakes and is not a doctor.** This app is built for emergencies, not as a replacement for professional medical care. Always seek professional medical help when possible. Nova F-R provides general first-aid guidance only — it is not a substitute for certified paramedic care, clinical treatment, or emergency services where available.

---

## Tech stack

- **UI:** Kotlin + Jetpack Compose
- **Local chat history:** Room (SQLite)
- **AI inference:** llama.cpp via JNI/C++
- **Model format:** GGUF, Q4_K_M quantization
- **Build:** GitHub Actions (Gradle + Android NDK)

---

## Installation

1. Go to the [Releases](../../releases) page
2. Download the latest `app-debug.apk`
3. Install it on your Android device (you may need to allow installs from unknown sources)
4. On first launch, the app will download the Atlas model (~986MB) — this is the *only* time internet is needed
5. That's it. Nova F-R now works fully offline.

---

## About the creator

Nova F-R was created by me, **Ismet Beljulji**, 17, born in Germany and currently living in France. Built as a solo passion project with the goal of putting real, offline emergency guidance into the hands of anyone who needs it — regardless of connectivity, location, or access to formal training.

**Supporting Organization:** [En Nova](https://www.linkedin.com/company/nova-stem/) — a youth-led educational initiative. Ismet serves as CS Mentor, Lead Engineer, and Researcher for Nova STEM. The Nova F-R name and branding are used with Nova STEM's permission under a formal collaboration agreement.

---

## License

This project is open source under the **Apache License 2.0**. Contributions, forks, and improvements from the community are welcome — this is meant to grow beyond one person.

---

## Acknowledgements

- [FirstAidQA dataset](https://huggingface.co/datasets/i-am-mushfiq/FirstAidQA) — Muna, Salvi, Mushfique, Abrar (Islamic University of Technology, Dhaka)
- [llama.cpp](https://github.com/ggerganov/llama.cpp) — Georgi Gerganov and contributors
- [Unsloth](https://github.com/unslothai/unsloth) — fine-tuning framework
- Qwen2.5 — Alibaba Cloud
- Nova STEM — for the name, the mission, and the support

---

*If this project helps someone in a moment that matters, it's done its job.*
