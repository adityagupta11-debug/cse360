"""Download project-local JavaFX/H2 dependencies for this computer (Python 3)."""
from pathlib import Path
import hashlib
import platform
import urllib.request

def main():
    system, arch = platform.system(), platform.machine().lower()
    platforms = {('Windows', 'amd64'): 'win', ('Windows', 'x86_64'): 'win',
                 ('Darwin', 'arm64'): 'mac-aarch64', ('Darwin', 'x86_64'): 'mac',
                 ('Linux', 'x86_64'): 'linux', ('Linux', 'aarch64'): 'linux-aarch64'}
    classifier = platforms.get((system, arch))
    if not classifier:
        raise SystemExit(f'Unsupported platform: {system} {arch}. See DEPENDENCIES.md.')
    destination = Path(__file__).resolve().parent / 'lib'
    destination.mkdir(exist_ok=True)
    base = 'https://repo.maven.apache.org/maven2/'
    artifacts = {f'javafx-{part}.jar': f'org/openjfx/javafx-{part}/21.0.8/javafx-{part}-21.0.8-{classifier}.jar'
                 for part in ('base', 'graphics', 'controls')}
    artifacts['h2.jar'] = 'com/h2database/h2/2.3.232/h2-2.3.232.jar'
    for name, relative in artifacts.items():
        url = base + relative
        with urllib.request.urlopen(url, timeout=60) as response:
            content = response.read()
        with urllib.request.urlopen(url + '.sha1', timeout=60) as response:
            expected = response.read().decode().strip().split()[0]
        if hashlib.sha1(content).hexdigest() != expected:
            raise RuntimeError(f'Checksum mismatch: {name}')
        temporary = destination / (name + '.tmp')
        temporary.write_bytes(content)
        temporary.replace(destination / name)
        print('Installed', name)
    print('Dependencies ready. Refresh TP1 in Eclipse and run Project > Clean.')

if __name__ == '__main__':
    main()
