import { describe, expect, it } from "vitest";
import { validateCorrection } from "../src/correction";
import { canonicalizeForHash } from "../src/requestHash";

describe("validateCorrection", () => {
  it("accepts choice question with options", () => {
    const result = validateCorrection(
      {
        content: "网络安全法施行时间是？",
        options: ["2016-11-07", "2017-06-01", "2017-01-01", "2018-06-01"],
        answer: "B",
        explanation: "2017年6月1日施行",
        reason: "选项是 Excel 序列号",
        confidence: 0.9,
      },
      {
        questionType: "单选题",
        content: "x",
        options: ["a", "b", "c", "d"],
        answer: "B",
        integrityToken: "",
        requestHash: "",
      },
    );
    expect(result.options).toHaveLength(4);
    expect(result.answer).toBe("B");
  });

  it("rejects choice question without options", () => {
    expect(() =>
      validateCorrection(
        {
          content: "题干",
          options: [],
          answer: "A",
          explanation: "",
          reason: "",
          confidence: 0.5,
        },
        {
          questionType: "单选题",
          content: "x",
          options: ["a", "b"],
          answer: "A",
          integrityToken: "",
          requestHash: "",
        },
      ),
    ).toThrow("BAD_OPTIONS");
  });
});

describe("canonicalizeForHash", () => {
  it("strips integrity fields and sorts keys", () => {
    const json = canonicalizeForHash({
      integrityToken: "t",
      requestHash: "h",
      content: "c",
      answer: "a",
      options: ["1"],
    });
    expect(json).toBe(JSON.stringify({ answer: "a", content: "c", options: ["1"] }));
    expect(json.includes("integrityToken")).toBe(false);
  });

  it("recursively sorts nested object keys", () => {
    const json = canonicalizeForHash({
      z: { b: 1, a: 2 },
      a: true,
      integrityToken: "x",
    });
    expect(json).toBe(JSON.stringify({ a: true, z: { a: 2, b: 1 } }));
  });
});

describe("validateCorrection fill/short", () => {
  it("allows empty options for fill questions", () => {
    const result = validateCorrection(
      {
        content: "空填____",
        options: [],
        answer: "答案",
        explanation: "",
        reason: "修正答案",
        confidence: 0.7,
      },
      {
        questionType: "填空题",
        content: "空填____",
        options: [],
        answer: "旧",
        integrityToken: "",
        requestHash: "",
      },
    );
    expect(result.options).toEqual([]);
    expect(result.answer).toBe("答案");
  });
});
