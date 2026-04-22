from fastapi import FastAPI, File, UploadFile, HTTPException
import pdfplumber
import io

app = FastAPI(title="SkillScout Python Parsing Microservice")

@app.get("/")
def read_root():
    return {"status": "online", "service": "PDF Extractor"}

@app.post("/extract-pdf")
async def extract_pdf(file: UploadFile = File(...)):
    if not file.filename.endswith(".pdf"):
        raise HTTPException(status_code=400, detail="File must be a PDF")
    
    try:
        text = ""
        pdf_bytes = await file.read()
        
        with pdfplumber.open(io.BytesIO(pdf_bytes)) as pdf:
            for page in pdf.pages:
                extracted = page.extract_text()
                if extracted:
                    text += extracted + "\n"
        
        # Returning only the pure text securely to the Java application.
        return {
            "status": "success",
            "extracted_text_length": len(text),
            "text": text
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
