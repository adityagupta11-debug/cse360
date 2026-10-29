"""Regression checks for submission acceptance mistakes found in the archive audit."""
import importlib.util
import json
from pathlib import Path
import struct
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('packager', Path(__file__).with_name('package_tp2.py'))
packager = importlib.util.module_from_spec(spec)
spec.loader.exec_module(packager)


def box(kind, content):
    return struct.pack('>I4s', len(content) + 8, kind) + content


def movie(seconds=180, version=0):
    timing = (bytes([version, 0, 0, 0]) + bytes(16 if version else 8)
              + struct.pack('>I', 1000)
              + (struct.pack('>Q', seconds * 1000) if version else struct.pack('>I', seconds * 1000)))
    tracks = b''.join(box(b'trak', box(b'mdia', box(b'hdlr', bytes(8) + kind)))
                      for kind in (b'vide', b'soun'))
    return box(b'moov', box(b'mvhd', timing) + tracks)


class PackagingRegressionTests(unittest.TestCase):
    def test_mov_and_mp4_timing_versions(self):
        with tempfile.TemporaryDirectory() as temp:
            for ext, version in [('.mp4', 0), ('.mov', 1)]:
                path = Path(temp) / ('summary' + ext)
                path.write_bytes(movie(180, version))
                self.assertEqual(packager.media_info(path),
                                 {'duration_seconds': 180, 'audio': True, 'video': True})

    def test_truncated_movie_is_rejected(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / 'bad.mp4'
            path.write_bytes(movie()[:-3])
            with self.assertRaises(ValueError):
                packager.media_info(path)

    def test_six_pairs_and_duration_are_required(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            folder = root / 'TP2 Planning and Tracking'
            folder.mkdir()
            for day in (21, 22, 24, 25, 27, 28):
                (folder / f'Plan and Status 2026-10-{day}.pdf').write_bytes(b'PDF fixture')
                (folder / f'Status Summary 2026-10-{day}.mov').write_bytes(movie(68 if day == 21 else 180))
            messages = []
            with patch.object(packager, 'ROOT', root), patch.object(packager, 'report',
                    side_effect=lambda ok, message: messages.append((bool(ok), message))), patch('builtins.print'):
                packager.check_contents(list(root.rglob('*')))
            self.assertTrue(next(ok for ok, text in messages if 'matching PDF/video pairs' in text))
            self.assertFalse(next(ok for ok, text in messages if '2026-10-21.mov: 68.00s' in text))
            (folder / 'Status Summary 2026-10-28.mov').unlink()
            messages.clear()
            with patch.object(packager, 'ROOT', root), patch.object(packager, 'report',
                    side_effect=lambda ok, message: messages.append((bool(ok), message))), patch('builtins.print'):
                packager.check_contents(list(root.rglob('*')))
            self.assertFalse(next(ok for ok, text in messages if 'matching PDF/video pairs' in text))


if __name__ == '__main__':
    unittest.main()
