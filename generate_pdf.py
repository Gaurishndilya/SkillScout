import subprocess
import sys

def install(package):
    subprocess.check_call([sys.executable, "-m", "pip", "install", package])

try:
    import reportlab
except ImportError:
    install('reportlab')

from reportlab.pdfgen import canvas

c = canvas.Canvas('e:/Projects/SkillScout/test_syllabus.pdf')
c.drawString(100, 750, 'Academic Syllabus: CS404 Intro to Software Engineering')
c.drawString(100, 730, 'Concepts: Microservices, Java Ecosystem, Modern Web Interfaces, Agile')
c.drawString(100, 710, 'Focus on relational data modeling with Database SQL.')
c.save()
print("PDF Generated Successfully")
