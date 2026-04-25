report:
	cd report && pdflatex report.tex
	cd report && pdflatex report.tex

	cd report && gs -sDEVICE=pdfwrite \
        -dCompatibilityLevel=1.4 \
        -dNOPAUSE \
        -dOptimize=true \
        -dQUIET \
        -dBATCH \
        -dRemoveUnusedFonts=true \
        -dRemoveUnusedImages=true \
        -dOptimizeResources=true \
        -dDetectDuplicateImages \
        -dCompressFonts=true \
        -dEmbedAllFonts=true \
        -dSubsetFonts=true \
        -dPreserveAnnots=true \
        -dPreserveMarkedContent=true \
        -dPreserveOverprintSettings=true \
        -dPreserveHalftoneInfo=true \
        -dPreserveOPIComments=true \
        -dPreserveDeviceN=true \
        -dMaxInlineImageSize=0 \
        -sOutputFile="report_compressed.pdf" \
        "report.pdf"

	cd report && mv report_compressed.pdf ИУ7-61Б-Гудкова-Арина-КуР-БД-РПЗ.pdf

clean:
	rm -f report/*.aux report/*.log report/*.toc report/*.out report/*.synctex.gz report/*.fdb_latexmk report/*.fls

.PHONY: all report clean
