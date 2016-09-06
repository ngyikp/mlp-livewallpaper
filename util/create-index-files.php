<?php

if (!isset($argv[1])) {
	echo 'Usage: '.__FILE__.' <path to folder>'."\n";
	echo 'Example: '.__FILE__.' ~/mlp-livewallpaper-assets/'."\n";
	exit;
}

$processor = new MlpLiveWallpaperCreateIndexFiles;
$processor->folder = $argv[1];
$processor->start();

class MlpLiveWallpaperCreateIndexFiles {
	public $folder;

	function start() {
		$handle = opendir($this->folder);
		if (!$handle) {
			throw new Exception('Can\'t open '.$this->folder);
		}

		while (false !== ($pony = readdir($handle))) {
			$this->handlePony($pony);
		}
		closedir($handle);

		echo 'Finished.'."\n";
	}

	private function handlePony($pony) {
		// Ignore dot files
		if (strpos($pony, '.') === 0) {
			return;
		}

		$path = $this->folder.DIRECTORY_SEPARATOR.$pony.DIRECTORY_SEPARATOR;

		// Folders only
		if (!is_dir($path)) {
			return;
		}

		file_put_contents($path.'index.htm', $this->getHtmlContentsForPony($path, $pony));

		echo 'Processed '.$pony."\n";
	}

	private function getHtmlContentsForPony($path, $pony) {
		$html = '';

		$handle = opendir($path);
		if (!$handle) {
			throw new Exception('Can\'t open '.$path."\n");
		}

		while (false !== ($file = readdir($handle))) {
			if (strpos($file, '.') === 0) {
				continue;
			}

			// Self o.O
			if ($file === 'index.htm') {
				continue;
			}

			$html .= '<li><a href="';
			$html .= $file;
			$html .= '"';
			$html .= "\n";
		}
		closedir($handle);

		return $html;
	}
}

